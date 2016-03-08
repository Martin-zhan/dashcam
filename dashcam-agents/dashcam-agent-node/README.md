#NodeJS Agent
## iqunxing-fx-dashcam-agent-node is a Node.js client for dashcam logging system
    suport: info, debug, error, fatal level log

## usage:
var agent = require('./agent');
var logger = agent.getLogger("win_node_hot_host");
logger.info("node_js_hot_host", "test message");