/**
 * Created by enousei on 9/17/15.
 */

var fs = require("fs"),
    filePath = "/etc/environment";

var data;
var name = "DEV", group = "DEV";

try{
    data = fs.readFileSync(filePath).toString()
    group = data.match('.ENV_GROUP=(.*)')[1];
    name = data.match('.ENV_NAME=(.*)')[1];
}catch(e){
    console.error(e);
}

exports.name = function(){
    return name;
}

exports.group = function(){
    return group;
}