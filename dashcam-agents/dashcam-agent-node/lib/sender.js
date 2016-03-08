/**
 * Created by enousei on 8/17/15.
 */

var kafka       = require('kafka-node');
var assert      = require('assert');
var util         = require('util');
var events      = require('events').EventEmitter;
var Serializer  = require("../lib/serializer");

/**
 * Log message sender.
 * @param options
 * @constructor
 */
var Sender = function(options){
    assert(options.zk != undefined, "zookeeper connection can't be empty");
    assert(options.topic != undefined, "kafka topic muse be set");
    assert(options.envTopic != undefined, "kafka env topic muse be set");
    this.client = new kafka.Client(options.zk);
    this.producer = new kafka.HighLevelProducer(this.client);
    this.topic = options.topic;
    this.envTopic = options.envTopic;
    this.serializer  = new Serializer();

    var self = this;
    this.producer.on('ready', function () {
        self.emit('ready');
    });
}

util.inherits(Sender, events.EventEmitter);

/**
 * Send log message chunk out
 * @param chunk message chunk
 * @param callback
 */
Sender.prototype.send = function(chunk, callback){
    var slf_producer = this.producer;
    var topic = this.topic;
    this.serializer.serialize(chunk, function(bytes){
        slf_producer.send([{topic: topic, messages: bytes}] , function(err, msg){
            callback(err, msg, chunk);
        });
    });
}

Sender.prototype.sendEnv = function(chunk, callback){
    var slf_producer = this.producer;
    var topic = this.envTopic;
    this.serializer.serialize(chunk, function(bytes){
        slf_producer.send([{topic: topic, messages: bytes}] , function(err, msg){
            callback(err, msg, chunk);
        });
    });
}

/**
 * Close connection
 */
Sender.prototype.close = function(){
    this.produce.close();
    this.client.close();
}

module.exports = Sender;


