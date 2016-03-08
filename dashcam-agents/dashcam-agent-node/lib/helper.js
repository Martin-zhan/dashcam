/**
 * Created by enousei on 8/18/15.
 */
var os      = require('os');
var ip      = require('./ip');
var Model   = require('../lib/model_types');
var length  = 8;

var self = module.exports = {
    random_int : function (min, max ){
        return Math.floor( Math.random() * ( max - min + 1 ) ) + min;
    },
    unique_id : function (){
        var timestamp = +new Date;
        var ts = timestamp.toString();
        var parts = ts.split( "" ).reverse();
        var id = "";

        for( var i = 0; i < length; ++i ) {
            var index = this.random_int( 0, parts.length - 1 );
            id += parts[index];
        }

        return new Number(id).value;
    },
    hostname: function (){
        return os.hostname();
    },
    hostIp: function(){
        return ip.address();
    },
    defaultChunk : function(options){
        var chunk = new Model.Chunk();
        chunk.appId = options.appId;
        chunk.env = options.env;
        chunk.envGroup = options.envGroup;
        chunk.hostName = this.hostname();
        chunk.hostIp = this.hostIp();

        chunk.logEvents = [];
        chunk.spans = [];
        chunk.metrics = [];
        chunk.events = [];
        return chunk;
    }
};