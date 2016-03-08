/**
 * Created by enousei on 8/17/15.
 */

var Metrics = function(){
    this.logEventCounter = 0;
    this.spanLogEventCounter = 0;
    this.spanCounter = 0;
}

Metrics.prototype.addEventLog = function(count){
    this.logEventCounter += count;
}

Metrics.prototype.getEventLogCount = function(){
    return this.logEventCounter;
}

Metrics.prototype.addSpanEventLog = function(count){
    this.spanLogEventCounter += count;
}

Metrics.prototype.getSpanEventLogCount = function(){
    return this.spanLogEventCounter;
}

Metrics.prototype.addSpan = function(count){
    this.spanCounter += count;
}

Metrics.prototype.getSpanCount = function(){
    return this.spanCounter;
}

var metrics = new Metrics();

module.exports = metrics;