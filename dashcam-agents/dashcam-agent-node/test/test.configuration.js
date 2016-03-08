
/**
 * Created by enousei on 8/21/15.
 */

var Configuration = require('../lib/configuration');

var configuration = new Configuration(9011);

describe("Configuration", function(){
    describe('#get', function () {
        it('should get config item successfully', function(done){
            configuration.on("ready", function(){
                var broker_list = configuration.get("dashcam.agent.kafka.brokerList", "localhost:9092");
                if(broker_list == "kafka1.s1.np.fx.dcfservice.com:9092,kafka2.s1.np.fx.dcfservice.com:9092,kafka3.s1.np.fx.dcfservice.com:9092"){
                    done();
                }else{
                    done("failed");
                }
            });
        });
    });
    /*describe('#change', function(){
        it('should listen change event successfully', function(done){
            configuration.on("change", function(stat, error){
                console.log(stat.key);
                console.log(stat.type);
                console.log(stat.value);
                done(error);
            });
        });
    });*/
});
