var singleton = function singleton(){
    var Log          = require("./lib/log");
    var Options      = require('./lib/options');
    var kafka        = require('kafka-node');
    var metrics      = require('./lib/metrics');
    var Sender       = require('./lib/sender');
    var Consumer     = require('./lib/consumer');
    var helper       = require('./lib/helper');
    var config       = require('./config.json');

    var options;
    var sender;
    var consumer;
    var initialized = false;

    var loggers = {};
    var ready = false;

    this.getLogger = function(name){
        if(!initialized){
            console.error("The dashcam logger not initialized");
            return null;
        }
        if(!loggers.hasOwnProperty(name)){
            loggers[name] = new Log(name, config.level, consumer);
        }
        return loggers[name];
    };

    this.init = function(cfg){
        if(initialized){
            return;
        }
        options = new Options(config);
        if(cfg){
            options = new Options(cfg);
        }
        sender = new Sender(options);
        consumer = new Consumer(options, sender)

        function send(){
            if(!ready){
                sender.sendEnv(helper.defaultChunk(options), function(err, msg){
                    if(err){
                        console.error(err);
                    }else{
                        ready = true;
                    }
                });
            }else{
                var chunk = consumer.get();
                while(chunk){
                    sender.send(chunk,  function(err, msg, data){
                        if (err) {
                            console.error(err);
                            consumer.push(chunk);
                        }else{
                            metrics.addEventLog(data.logEvents.length);
                            for(var span in data.spans){
                                metrics.addSpanEventLog(span.logEvents.length);
                            }
                            metrics.addSpan(data.spans.length);
                        }
                    });

                    chunk = consumer.get();
                }
            }
        }

        sender.on("ready", function(){
            setInterval(send, options.interval);
        });

        initialized = true;
    }

    if(singleton.caller != singleton.getInstance){
        throw new Error("This object cannot be instanciated");
    }
}

/* ************************************************************************
 SINGLETON CLASS DEFINITION
 ************************************************************************ */
singleton.instance = null;

/**
 * Singleton getInstance definition
 * @return singleton class
 */
singleton.getInstance = function(){
    if(this.instance === null){
        this.instance = new singleton();
    }
    return this.instance;
}

module.exports = singleton.getInstance();