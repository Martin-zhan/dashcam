/**
 * Created by enousei on 8/18/15.
 */

var LogLevel = require('./model_types').LogLevel;

/**
 * Initialize log options with default value
 * @param options config
 * @constructor
 */
var Options = function(options){

    function has(option){
        return option != undefined && option != null;
    }

    this.appId = has(options.appId) ? options.appId : 0;

    this.env = "DEV";
    if(has(process.env.ENV_NAME)){
        this.env = process.env.ENV_NAME;
    }else if(has(options.env)){
        this.env = options.env;
    }

    this.zk = "zk3.s1.np.fx.dcfservice.com:2181,zk2.s1.np.fx.dcfservice.com:2181,zk1.s1.np.fx.dcfservice.com:2181";
    if(has(options.zk)){
        this.zk = options.zk;
    }

    if(this.env.toUpperCase() == "PRODUCT"){
        this.zk = "zk1.s1.fx.dcfservice.com:2181,zk2.s1.fx.dcfservice.com:2181,zk3.s1.fx.dcfservice.com:2181,zk4.s1.fx.dcfservice.com:2181,zk5.s1.fx.dcfservice.com:2181";
    }

    this.topic = has(options.topic) ? options.topic : "com.dcf.iqunxing.fx.dashcam";
    this.envTopic = has(options.envTopic) ? options.envTopic : "com.dcf.iqunxing.fx.dashcam.env";

    this.envGroup = "DEV";
    if(has(process.env.ENV_GROUP)){
        this.envGroup = process.env.ENV_GROUP;
    }else if(has(options.envGroup)){
        this.envGroup = options.envGroup;
    }

    console.log(this.env);
    console.log(this.envGroup);

    this.level = LogLevel.INFO;
    if(has(options.level)){
        var level_Str = options.level.toUpperCase();
        switch (level_Str){
            case "DEBUG":
                this.level = LogLevel.DEBUG;
                break;
            case "ERROR":
                this.level = LogLevel.ERROR;
                break;
            case "FATAL":
                this.level = LogLevel.FATAL;
                break;
            case  "WARN":
                this.level = LogLevel.WARN;
                break;
        }
    }

    this.capacity = has(options.capacity) ? options.capacity : 50;
    this.interval = has(options.interval) ? options.interval : 500;
    this.maxInterval = has(options.maxInterval) ? options.maxInterval : 2000;

    this.maxCapacity = has(options.maxCapacity) ? options.maxCapacity : 2000;

    //must be a float number
    this.dropRatio = has(options.dropRatio) ? options.dropRatio : 0.005;
}

module.exports = Options;