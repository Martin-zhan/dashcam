/**
 * Created by enousei on 8/17/15.
 */
var TFramedTransport    = require('thrift/lib/thrift/transport').TFramedTransport;
var TBinaryProtocol     = require('thrift/lib/thrift/protocol').TBinaryProtocol;

/**
 * Provide serializer Thrift-model to byte array for kafka transport
 * @constructor
 */
var Serializer = function() {
    this.transport = new TFramedTransport();
    this.protocol = new TBinaryProtocol(this.transport);
}

/**
 * Serialize the specified thrift {Model_types.Chunk} to byte array.
 * On completed, onFlush callback will be invoke with byte buffer
 * @param chunk
 * @param onFlush
 */
Serializer.prototype.serialize = function(chunk, onFlush){
    var buffer = new Buffer(chunk);
    this.transport.buffer = buffer;
    this.transport.onFlush = function(byteArray){
        onFlush(byteArray.slice(4));
    };
    chunk.write(this.protocol);
    this.transport.flush();
}

module.exports = Serializer;

