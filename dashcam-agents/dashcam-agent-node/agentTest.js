/**
 * Created by Yuan on 2015/9/21.
 */

var agent = require('./agent');
agent.init({
  appId: 9008,
  level: "INFO"
});
var logger = agent.getLogger("win_node_hot_host");

for(var i = 0; i< 49; i++){
    logger.info("node_js_hot_host", "test message" + i);
}


console.info("done");