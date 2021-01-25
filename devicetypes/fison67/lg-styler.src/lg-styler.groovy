/**
 *  LG Styler(v.0.0.3)
 *
 * MIT License
 *
 * Copyright (c) 2020 fison67@nate.com
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
*/
 
import groovy.json.JsonSlurper
import groovy.transform.Field

@Field
STATE_VALUE = [
	0: [val: "@ST_STATE_POWER_OFF_W", str: ["EN":"OFF", "KR":"OFF"] ],
	1: [val: "@ST_STATE_INITIAL_W", str: ["EN":"INITIAL", "KR":"대기 중"] ],
	2: [val: "@ST_STATE_RUNNING_W", str: ["EN":"RUNNING", "KR":"스타일링 중"] ],
	3: [val: "@ST_STATE_PAUSE_W", str: ["EN":"PAUSE", "KR":"일시정지 중"] ],
	4: [val: "@ST_STATE_END_W", str: ["EN":"END", "KR":"종료 상태"] ],
	5: [val: "@ST_STATE_ERROR_W", str: ["EN":"ERROR", "KR":"에러 발생"] ],
	6: [val: "@ST_STATE_SMART_DIAGNOSIS_W", str: ["EN":"SMART DIAGNOSIS", "KR":"스마트 진단 중"] ],
	7: [val: "@ST_STATE_NIGHTDRY_W", str: ["EN":"NIGHTDRY", "KR":"보관 중"] ],
	8: [val: "@ST_STATE_RESERVE_W", str: ["EN":"RESERV", "KR":"예약 중"] ],
	50: [val: "@ST_STATE_RUNNING_PRESTEAM_W", str: ["EN":"PRESTEAM", "KR":"스팀 준비 중"] ],
	51: [val: "@ST_STATE_RUNNING_STEAM_W", str: ["EN":"STEAM", "KR":"스팀 중"] ],
	52: [val: "@ST_STATE_RUNNING_STEAM_W", str: ["EN":"STEAM", "KR":"스팀 중"] ],
	53: [val: "@ST_STATE_RUNNING_STEAM_W", str: ["EN":"STEAM", "KR":"스팀 중"] ],
	54: [val: "@ST_STATE_RUNNING_DRYING_W", str: ["EN":"DRY", "KR":"건조 중"] ],
	55: [val: "@ST_STATE_RUNNING_DRYING_W", str: ["EN":"DRY", "KR":"건조 중"] ],
	56: [val: "@ST_STATE_RUNNING_DRYING_W", str: ["EN":"DRY", "KR":"건조 중"] ],
	57: [val: "@ST_STATE_RUNNING_STERILIZE_W", str: ["EN":"STERILIZE", "KR":"살균 중"] ],
	58: [val: "@ST_STATE_END_W", str: ["EN":"END", "KR":"종료 상태"] ],
	98: [val: "@ST_STATE_FOTA_W", str: ["EN":"UPDATE", "KR":"업데이트 중"] ]
]

metadata {
	definition (name: "LG Styler", namespace: "fison67", author: "fison67") {
        capability "Sensor"
        capability "Switch"
        
        command "setStatus"
        
		attribute "leftMinute", "number"
        attribute "curState", "string"
        attribute "processState", "string"
        attribute "course", "string"
	}

	simulator {
	}
    
	preferences {
        input name: "language", title:"Select a language" , type: "enum", required: true, options: ["EN", "KR"], defaultValue: "KR", description:"Language for DTH"
	}

	tiles(scale: 2) {
		
        multiAttributeTile(name:"switch", type: "generic", width: 6, height: 2){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState("on", label:'${name}', backgroundColor:"#00a0dc", icon:"https://github.com/fison67/LG-Connector/blob/master/icons/lg-washer.png?raw=true")
                attributeState("off", label:'${name}', backgroundColor:"#ffffff",  icon:"https://github.com/fison67/LG-Connector/blob/master/icons/lg-washer.png?raw=true")
			}
            
			tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'Updated: ${currentValue}')
            }
		}
        valueTile("processState_label", "", decoration: "flat", width: 3, height: 1) {
            state "default", label:'Process State'
        }
        valueTile("processState", "device.processState", decoration: "flat", width: 3, height: 1) {
            state "default", label:'${currentValue}'
        }
        valueTile("course_label", "", decoration: "flat", width: 3, height: 1) {
            state "default", label:'Course'
        }
        valueTile("course", "device.course", decoration: "flat", width: 3, height: 1) {
            state "default", label:'${currentValue}'
        }
        valueTile("leftTime_label", "", decoration: "flat", width: 3, height: 1) {
            state "default", label:'Left Time'
        }
        valueTile("leftTime", "device.leftTime", decoration: "flat", width: 3, height: 1) {
            state "default", label:'${currentValue}'
        }
        
        
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def updated() {
}

def setInfo(String app_url, String address) {
	log.debug "${app_url}, ${address}"
	state.app_url = app_url
    state.id = address
}

def setData(dataList){
	for(data in dataList){
        state[data.id] = data.code
    }
}

def setStatus(data){
	log.debug "Update >> ${data.key} >> ${data.data}"
    
    def jsonObj = new JsonSlurper().parseText(data.data)
    
    if(jsonObj.State != null){
    	if(jsonObj.State.value as int == 0){
        	sendEvent(name:"switch", value: "off")
        }else{
        	sendEvent(name:"switch", value: "on")
        }
        sendEvent(name:"curState", value: STATE_VALUE[jsonObj.State.value as int]["str"][language])
    }
    
    if(jsonObj.ProcessState != null){
    	def value = STATE_VALUE[jsonObj.ProcessState.value as int]["str"][language]
        if(jsonObj.State.value as int == 0){
        	value = "OFF"
        }
    	sendEvent(name:"processState", value: value)
    }
    
    if(jsonObj.Remain_Time_H != null){
    	state.remainTimeH = changeTime(jsonObj.Remain_Time_H.rValue)
	}
    if(jsonObj.Remain_Time_M != null){
    	state.remainTimeM = changeTime(jsonObj.Remain_Time_M.rValue)
	}
   
    sendEvent(name:"leftTime", value: state.remainTimeH + ":" + state.remainTimeM + ":00")
    if(jsonObj.Remain_Time_H != null){
    	sendEvent(name:"leftMinute", value: jsonObj.Remain_Time_H.value * 60 + jsonObj.Remain_Time_M.value)
    }

    updateLastTime();
}

def changeTime(time){
	if(time < 10){
    	return "0" + time
    }
    return "" + time
}

def updateLastTime(){
	def now = new Date().format("yyyy-MM-dd HH:mm:ss", location.timeZone)
    sendEvent(name: "lastCheckin", value: now, displayed:false)
}

def makeCommand(type, value){
    def body = [
        "id": state.id,
        "cmd": type,
        "data": value
    ]
    def options = makeCommand(body)
    sendCommand(options, null)
}

def makeCommand(body){
	def options = [
     	"method": "POST",
        "path": "/tv/control",
        "headers": [
        	"HOST": state.app_url,
            "Content-Type": "application/json"
        ],
        "body":body
    ]
    return options
}

def sendCommand(options, _callback){
	def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: _callback])
    sendHubCommand(myhubAction)
}
