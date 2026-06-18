class ViewConfig {
  constructor(viewName, dispatcher, config = {}) {
    this.viewName = viewName;
    this.dispatcher = dispatcher;
    this.urlInicio = config.urlInicio;
    this.urlCierre = config.urlCierre;
    this.dataInicio = config.dataInicio;
    this.onError = config.onError;
    this.onException = config.onException;
    this.handlers = new Map();
    this.cleanup = null;
    this.started = false;
  }

  handler(commandName, handlerFn) {
    if (!this.handlers.has(commandName)) {
      this.handlers.set(commandName, []);
    }
    this.handlers.get(commandName).push(handlerFn);
    return handlerFn;
  }

  _start() {
    if (this.started && this.cleanup) {
      this.cleanup();
    }

    const handlersObj = {};
    this.handlers.forEach((handlerArray, commandName) => {
      handlersObj[commandName] = (parametro) => {
        handlerArray.forEach(handler => handler(parametro));
      };
    });

    this.cleanup = this.dispatcher.mapFunctions(handlersObj, {
      onError: this.onError,
      onException: this.onException
    });

    if (this.urlInicio) {
      this.dispatcher.submit(this.urlInicio, this.dataInicio || {});
    }

    this.started = true;
  }

  _finish() {
    if (this.urlCierre) {
      this.dispatcher.submit(this.urlCierre, {});
      this.urlCierre = null;
    }

    if (this.cleanup) {
      this.cleanup();
      this.cleanup = null;
    }

    this.started = false;
    this.dispatcher.views.delete(this.viewName);
  }
}

class CommandDispatcher {
  constructor() {
    this.contexts = new Map();
    this.views = new Map();
  }

  registerView(viewName, config = {}) {
    const view = new ViewConfig(viewName, this, config);
    this.views.set(viewName, view);
    return view;
  }

  startView(viewConfig) {
    viewConfig._start();
  }

  finishView(viewConfig) {
    viewConfig._finish();
  }

  mapFunctions(handlers, options = {}) {
    const contextId = Symbol('context');
    const functionsMap = new Map();

    Object.entries(handlers).forEach(([name, handler]) => {
      if (typeof handler === 'function') {
        functionsMap.set(name, handler);
      }
    });

    this.contexts.set(contextId, {
      functions: functionsMap,
      onError: options.onError,
      onException: options.onException
    });

    return () => this.contexts.delete(contextId);
  }

  async submit(url, data = {}, method = 'POST') {
    const normalizedMethod = String(method || 'POST').toUpperCase();
    const bodyData = this._toUrlEncoded(data);
    const requestUrl = normalizedMethod === 'GET' && bodyData
      ? `${url}${url.includes('?') ? '&' : '?'}${bodyData}`
      : url;

    const requestOptions = {
      method: normalizedMethod,
      credentials: 'include'
    };

    if (normalizedMethod !== 'GET') {
      requestOptions.headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
      requestOptions.body = bodyData;
    }

    try {
      const response = await fetch(requestUrl, requestOptions);
      const status = response.status;
      const text = await response.text();

      if (status === 299) {
        this._notifyException(text);
        return;
      }

      if (status < 200 || status > 299) {
        this._notifyError(status, this._formatHttpError(status, text));
        return;
      }

      if (!text || text.trim() === '') {
        return;
      }

      const json = JSON.parse(text);
      if (json.commands && Array.isArray(json.commands)) {
        this._processCommands(json.commands);
      }
    } catch (error) {
      this._notifyError(0, error.message);
    }
  }

  connectEvents(url = '/eventos/stream') {
    if (!window.EventSource) {
      console.warn('El navegador no soporta eventos del servidor');
      return null;
    }

    const source = new EventSource(url);
    source.onmessage = (event) => {
      if (!event.data) {
        return;
      }

      try {
        const json = JSON.parse(event.data);
        if (json.commands && Array.isArray(json.commands)) {
          this._processCommands(json.commands);
        }
      } catch (error) {
        this._notifyError(0, 'Evento inválido: ' + error.message);
      }
    };
    source.onerror = () => {
      console.warn('Conexion de eventos interrumpida, el navegador intentara reconectar');
    };
    return source;
  }

  _toUrlEncoded(data) {
    if (typeof data === 'string') {
      return data;
    }

    const params = new URLSearchParams();
    Object.entries(data).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        params.append(key, value);
      }
    });
    return params.toString();
  }

  _processCommands(commands) {
    commands.forEach(command => {
      const matchingHandlers = [];
      this.contexts.forEach(context => {
        const handler = context.functions.get(command.id);
        if (handler) {
          matchingHandlers.push(handler);
        }
      });

      matchingHandlers.forEach(handler => handler(command.parametro));
      if (matchingHandlers.length === 0) {
        console.warn("Comando sin handler:", command.id, command.parametro);
      }
    });
  }

  _notifyError(status, text) {
    let notified = false;
    this.contexts.forEach(context => {
      if (typeof context.onError === 'function') {
        context.onError(status, text);
        notified = true;
      }
    });

    if (!notified) {
      console.error("Error HTTP:", status, text);
    }
  }

  _formatHttpError(status, text) {
    if (status === 404) {
      return 'No se encontró el recurso solicitado';
    }
    if (status === 405) {
      return 'La accion solicitada no esta disponible';
    }
    if (status >= 500) {
      return 'Ocurrio un error interno. Intente nuevamente';
    }

    try {
      const json = JSON.parse(text);
      return json.message || json.error || text;
    } catch (error) {
      return text || 'Ocurrio un error al procesar la solicitud';
    }
  }

  _notifyException(mensaje) {
    let notified = false;
    this.contexts.forEach(context => {
      if (typeof context.onException === 'function') {
        context.onException(mensaje);
        notified = true;
      }
    });

    if (!notified) {
      alert(mensaje);
    }
  }
}

const dispatcher = new CommandDispatcher();

export default dispatcher;
export { dispatcher as CommandDispatcher };

window.CommandDispatcher = dispatcher;
globalThis.CommandDispatcher = dispatcher;
