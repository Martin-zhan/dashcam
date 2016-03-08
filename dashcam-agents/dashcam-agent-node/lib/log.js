/**
 * Created by enousei on 8/17/15.
 */

var Model       = require('./model_types');
var Consumer    = require("./consumer");

var Log = function(name, level, consumer){
    this.name = name;
    this.level = level;
    this.consumer = consumer;
}


Log.prototype.writeLog = function(logLevel, title, message, attrs){
    if(this.level > logLevel){
        return false;
    }
    var logEvent = new Model.LogEvent();
    logEvent.id = 1;
    logEvent.logLevel = logLevel;
    logEvent.logType = Model.LogType.APP;
    logEvent.createdTime = new Date().getTime();
    logEvent.source = this.name;
    logEvent.title = title == undefined ? "NA" : title;
    logEvent.message = message == undefined ? "NA" : message;
    if(attrs != null){
        logEvent.attributes = attrs;
    }

    return this.consumer.put(logEvent);
}

Log.prototype.debug = function(title, message, tags){
    return this.writeLog(Model.LogLevel.DEBUG, title, message, tags)
}

/**
 * Record a info level log with specified title and tags
 * @param title
 * @param message
 * @param tags: {tag1=value1, tag2= value2}
 */
Log.prototype.info = function(title, message, tags){
    if(tags && typeof tags != 'object'){
        throw new Error("Tags param must be a map object");
    }
    return this.writeLog(Model.LogLevel.INFO, title,  message, tags);
}

/**
 * Record a error level log
 * @param title
 * @param error
 * @param tags: {tag1=value1, tag2= value2}
 */
Log.prototype.error = function(title, error, tags){
    var msg;
    if(error){
        if(error instanceof Error){
            msg = error.stack;
        }else{
            msg = error;
        }
    }

    return this.writeLog(Model.LogLevel.ERROR, title, msg, tags);
}

Log.prototype.warn = function(title, message, tags){
    return this.writeLog(Model.LogLevel.WARN, title, message, tags);
}

Log.prototype.fatal = function(title, error, tags){
    var msg;
    if(error){
        if(typeof error == 'Error'){
            msg = error.stack;
        }else{
            msg = error;
        }
    }

    return this.writeLog(Model.LogLevel.FATAL, msg, title, tags);
}

module.exports = Log;

