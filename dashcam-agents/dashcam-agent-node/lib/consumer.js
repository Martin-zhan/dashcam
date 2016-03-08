/**
 * Created by enousei on 8/17/15.
 */

var Model   = require('./model_types');
var helper  = require('./helper');

/**
 * Consume the log message.
 * When log capacity is satisfied, chunk will be send out.
 * @param options
 * @constructor
 */
function Consumer(options){
    this.options = options;
    this.size = 0;
    this.chunks = new Array();
    this.last = new Date().getTime();

    this.chunk = helper.defaultChunk(options);
}

/**
 * Put the new log into chunk
 * @param tbase
 *      one of [LogEvent, MetricEvent, Span, Event] instance,
 *      which wrap a new log message
 */
Consumer.prototype.put = function(tbase){
    if(tbase instanceof Model.LogEvent) {
        this.chunk.logEvents.push(tbase);
        this.size ++;
    }else if(tbase instanceof Model.MetricEvent){
        this.chunk.metrics.push(tbase);
        this.size++;
    }else if(tbase instanceof Model.Span){
        this.chunk.spans.push(tbase);
        if(tbase.logEvents){
            this.size =+ tbase.logEvents.length;
        }
    }else if(tbase instanceof Model.Event){
        this.chunk.events.push(tbase);
        this.size ++;
    }
    if(this.size >= this.options.capacity){
        this.chunks.push(this.chunk);
        this.chunk = helper.defaultChunk(this.options);
        this.size = 0;
    }
    return true;
}

Consumer.prototype.get = function(){
    var now = new Date().getTime();
    if(this.chunks.length <= 0) {
        if (this.size > 0 && (now - this.last) > this.options.maxInterval) {
            this.chunks.push(this.chunk);
            this.chunk = helper.defaultChunk(this.options);
            this.size = 0;
        }
    }
    var ck = this.chunks.shift();
    if(ck){
        this.last = now;
    }
    return ck;
}

Consumer.prototype.push = function(ck){
    if(this.chunks.length <= (this.options.maxCapacity - 1)) {
        this.chunks.push(ck);
    }
}

module.exports = Consumer;