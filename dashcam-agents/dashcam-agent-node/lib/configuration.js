/**
 * Created by enousei on 8/18/15.
 */

var Zookeeper = require('node-zookeeper-client'),
    fs = require('fs-extra'),
    path = require('path'),
    home = process.env[(process.platform == 'win32') ? 'USERPROFILE' : 'HOME'],
    util = require("util"),
    events = require('events').EventEmitter;
    env = process.env.env ? process.env.env : "DEV";

/**
 * Local cache of configuration under ${usr.home}/iqunxing/node path
 * @param appId
 * @constructor
 */
var LocalCache = function(appId){
    this.cache = {};

    this.localPath = this.home() + "/iqunxing/node/" + appId + "-config.json";

    this.init();
}

util.inherits(LocalCache, events.EventEmitter);

LocalCache.prototype.home = function(username) {
    return username ? path.resolve(path.dirname(home), username) : home;
}

LocalCache.prototype.init = function(){
    fs.ensureFileSync(this.localPath);
    var data = fs.readJsonSync(this.localPath);
    if (data) {
        this.cache = data;
    }
}

LocalCache.prototype.flush = function(){
    var self = this;
    fs.writeJson(this.localPath, this.cache, function(error){
        if(!error) {
            self.emit("error", error);
        }
    });
}

LocalCache.prototype.get = function(key){
    return this.cache[key];
}

LocalCache.prototype.set = function(key, value){
    this.cache[key] = value;
}

LocalCache.prototype.delete = function(key){
    delete this.cache[key];
}

LocalCache.prototype.check = function(keys){
    for(var key in this.cache){
        if(keys.indexOf(key) < 0){
            this.delete(key)
        }
    }
}

/**
 * Configuration service client.
 * Sync the config items from zookeeper cluster
 * @param appId default appId
 * @constructor
 */
var Configuration = function(appId){
    this.appId = appId;
    this.cache = new LocalCache(this.appId);
    this.zk = "zk3.s1.np.fx.dcfservice.com:2181,zk2.s1.np.fx.dcfservice.com:2181,zk1.s1.np.fx.dcfservice.com:2181";
    if(env.toUpperCase() == "PRODUCT"){
        this.zk = "zk1.s1.fx.dcfservice.com:2181,zk2.s1.fx.dcfservice.com:2181,zk3.s1.fx.dcfservice.com:2181,zk4.s1.fx.dcfservice.com:2181,zk5.s1.fx.dcfservice.com:2181";
    }
    this.zk_root = "/iqunxing/config";
    this.ready = 0;
    this.isReady = false;
    this.keys = new Array();

    this.zookeeper = Zookeeper.createClient(this.zk);
    this.zookeeper.connect();

    this.init();

    setInterval(function(){
        self.cache.flush();
    }, 5* 60 * 1000);
}

util.inherits(Configuration, events.EventEmitter);

/**
 * Initialize the configuration connect
 */
Configuration.prototype.init = function(){
    var self = this;
    var path = this.zk_root + "/" + this.appId + "/" + env;
    self.zookeeper.once('connected', function(){
        self.zookeeper.exists(path, function(error) {
            if (!error) {
                self.zookeeper.getChildren(path, function (error, children) {
                    if (!error && children){ //successfully
                        self.ready = children.length;
                        children.forEach(function (node) {
                            self.keys.push(node);
                            self.update(node, path + "/" + node);
                        });
                    }
                });
            }
        });
    });
}

/**
 * Update the node(key) on the specified zk path.
 * @param node zk node name
 * @param path zk path which indicate this node(key)
 */
Configuration.prototype.update = function(node, path){
    var self = this;
    self.zookeeper.getData(path,
        function (event, error) {
            if(event.type == Zookeeper.Event.NODE_DATA_CHANGED){ //Notify change to user
                self.update(node, path);
            }
            if(event.type == Zookeeper.Event.NODE_DELETED){
                self.cache.delete(node);
                self.emit("change", {key: node, type: -1}, error); //Notify delete to user
            }
        },
        function (error, data) {
            if (data) {
                self.cache.set(node, data.toString("UTF-8"));
                if(self.isReady) {
                    self.emit("change", {key: node, type: 1, value: self.cache.get(node)}, error);
                }
            }
            self.ready --;
            if(self.ready == 0){ //Initialize completed
                self.emit('ready');
                self.isReady = true;
                self.cache.check(self.keys);
            }
        });
}

/**
 * Query the config value
 * @param key config key
 * @param defaultVal default value
 * @returns {string}
 */
Configuration.prototype.get = function(key, defaultVal){
    var value = this.cache.get(key);
    return value == undefined ? defaultVal : value;
}

/**
 * Get the zookeeper connection string
 * @returns {string|string}
 */
Configuration.prototype.source = function(){
    return this.zk;
}

module.exports = Configuration;
