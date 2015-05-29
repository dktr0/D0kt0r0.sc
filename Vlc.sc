Vlc {

	classvar <config; // symbol naming current configuration
	classvar <latency;
	classvar <speakers; // dictionary of speaker sets
	classvar <proxySpace;
	classvar <mainGroup,<outputGroup;
	classvar <mainBus;
	classvar <delayedSynth,<noTablaSynth,<tablaSynth;
	classvar recSynth,recBuffer;

	*meow {
		| cfg = \stereo |
		config = cfg;
		latency = 0.135;
		Server.internal.options.device = "JackRouter";
		Server.internal.options.numOutputBusChannels = 34;
		Server.internal.options.numInputBusChannels = 2;
		Server.internal.options.numAudioBusChannels = 256;
		Server.default = Server.internal;
		proxySpace = ProxySpace.new.push;
		proxySpace.fadeTime = 2;
		Server.internal.waitForBoot( { Vlc.afterBoot; "meow".postln; });
	}

	*afterBoot {
		proxySpace.group = mainGroup = Group.head;
		outputGroup = Group.tail;
		mainBus = Bus.audio(Server.default,32);
		Vlc.spaces;
		Vlc.nodes;
		Vlc.synths;
		Vlc.outputs;
		Vlc.jack;
		Vlc.record;
	}

	*quit {
		Server.default.quit;
	}

	*latency_ { |x|
		latency=x;
		proxySpace.push;
		~latency = latency;
		proxySpace.pop;
	}

	*spaces {
		if(config == \liveLab,{ speakers = Dictionary[
			\main -> ([13,14]-1),
			\left -> ((1..4)-1),
			\right -> ((5..8)-1),
			\back -> ((9..12)-1),
			\leftUp -> ((15..18)-1),
			\rightUp -> ((19..22)-1),
			\distant -> ([23,24]-1),
			\all -> ((1..24)-1),
			\nomain -> (((1..12)++(15..24))-1),
			\up -> ((15..22)-1),
			\down -> ((1..12)-1),
			\01 -> ([0,1])
		]});
		if(config == \stereo,{ speakers = Dictionary[
			\main -> ([13,14]-1),
			\left -> ((1..4)-1),
			\right -> ((5..8)-1),
			\back -> ((9..12)-1),
			\leftUp -> ((15..18)-1),
			\rightUp -> ((19..22)-1),
			\distant -> ([23,24]-1),
			\all -> ((1..24)-1),
			\nomain -> (((1..12)++(15..24))-1),
			\up -> ((15..22)-1),
			\down -> ((1..12)-1),
			\01 -> [0,1]
		]});
		if(config == \nime2015, { speakers = Dictionary[
			\stage -> ((1..8)-1),
			\left -> ((9..12)-1),
			\right -> ((13..16)-1),
			\back -> ((17..24)-1),
			\up -> ((25..32)-1),
			\01 -> [0,1]
		]});

/* details of stage speakers at NIME:
1		55		top narrow speakers left
2		53		top narrow speakers right
3		54		top narrow speakers centre
4		32&33	stage centre (4 spkrs)
5		31		stage left (2 spkrs)
6		34		stage right (2 spkrs)
7		17		high-wide onstage speaker left
8		22		high-wide onstage speaker right
*/

	speakers.keysValuesDo( { | key,value | Pdefn(key,Pxrand(value+mainBus.index,inf)); });
	}

	*delay {
		| signal,time |
		^DelayN.ar(signal,time-latency,time-latency);
	}

	*follow {
		| signal,time,db |
		^(DelayN.ar(~env.ar,time-latency,time-latency) *
			signal * (db.dbamp));
	}

	*nodes {
		~test = { SinOsc.ar(440,mul:-20.dbamp) };
		~latency = latency;
		~tabla = -100;
		~tabla.fadeTime = 4;
		~stereo = {SoundIn.ar([1,0])};
		~mono = {SoundIn.ar(0)+SoundIn.ar(1)};
		~env = {Amplitude.ar(SoundIn.ar(0)+SoundIn.ar(1),0.003,0.18)};
		~geF = 38.midicps;
		~giF = 50.midicps;
		~naF = 74.midicps;
		~tunF = 64.midicps;
		~geQ = 40;
		~giQ = 40;
		~naQ = 400;
		~tunQ = 200;
		~geG = 0;
		~giG = 12;
		~naG = 27;
		~tunG = 18;
		~geA = { Resonz.ar(SoundIn.ar(0),~geF.kr,bwr:1/~geQ.kr,mul:~geG.ar) };
		~giA = { Resonz.ar(SoundIn.ar(0),~giF.kr,bwr:1/~giQ.kr,mul:~giG.ar) };
		~naA = { Resonz.ar(SoundIn.ar(0),~naF.kr,bwr:1/~naQ.kr,mul:~naG.ar) };
		~tunA = { Resonz.ar(SoundIn.ar(0),~tunF.kr,bwr:1/~tunQ.kr,mul:~tunG.ar) };
		~ge = { Amplitude.ar(~geA.ar,0.001,0.03) };
		~gi = { Amplitude.ar(~giA.ar,0.001,0.03) };
		~na = { Amplitude.ar(~naA.ar,0.001,0.03) };
		~tun = { Amplitude.ar(~tunA.ar,0.001,0.03) };
	}

	*synths {
		SynthDef(\dly,{
			arg sustain=0.5, dly=0.25, amp=0.1, out=0;
			var audio,env;
			env = Env.linen(0.005,dly-0.01,0.005);
			env = EnvGen.ar(env);
			audio = (SoundIn.ar(0)+SoundIn.ar(1))*env;
			audio = DelayN.ar(audio,dly,dly);
			env = EnvGen.ar(Env.linen(0.01,sustain,0.01),doneAction:2);
			Out.ar(out,audio*env);
		}).add;
		thisProcess.interpreter.executeFile("/Users/ogbornd/d0kt0r0.sc/synths.sc");
	}

	*outputs {
		if(delayedSynth.notNil,{delayedSynth.free});
		if(noTablaSynth.notNil,{noTablaSynth.free});
		if(tablaSynth.notNil,{tablaSynth.free});
		if( config == \stereo,{
			delayedSynth = SynthDef(\delayedSynth,{
				var left = Vlc.sumOfAudioBuses([0,1,2,3,8,9,12,14,15,16,17,22]+mainBus.index);
				var right = Vlc.sumOfAudioBuses([4,5,6,7,10,11,13,18,19,20,21,23]+mainBus.index);
				Out.ar(0,DelayN.ar([left,right],1.0,~latency.kr,-6.dbamp));
			}).play(target:outputGroup);
			noTablaSynth = SynthDef(\noTablaSynth,{
				var left = Vlc.sumOfAudioBuses([0,1,2,3,8,9,14,15,16,17,22]+mainBus.index);
				var right = Vlc.sumOfAudioBuses([4,5,6,7,10,11,18,19,20,21,23]+mainBus.index);
				Out.ar(32,[left,right]/2);
			}).play(target:outputGroup);
			tablaSynth = SynthDef(\tablaSynth,{
				var left = SoundIn.ar(0);
				var right = SoundIn.ar(1);
				var env = Lag.ar(K2A.ar(~tabla.kr.dbamp),lagTime:4);
				Out.ar(12,[left,right]*env); // ???
			}).play(target:outputGroup);
		});
	}

	*sumOfAudioBuses { |buses| ^buses.inject(K2A.ar(0),function:{|x,y|x+In.ar(y);}); }

	*jack {
		if(config == \stereo,{
			"jack_connect jacktrip:receive_1 sclang:in1".systemCmd;
			"jack_connect jacktrip:receive_2 sclang:in2".systemCmd;
			"jack_connect sclang:out33 jacktrip:send_1".systemCmd;
			"jack_connect sclang:out34 jacktrip:send_2".systemCmd;
			Vlc.connectMainOuts(2);
		});
		if(config == \liveLab, {
			"jack_connect jacktrip:receive_1 sclang:in1".systemCmd;
			"jack_connect jacktrip:receive_2 sclang:in2".systemCmd;
			"jack_connect sclang:out33 jacktrip:send_1".systemCmd;
			"jack_connect sclang:out34 jacktrip:send_2".systemCmd;
			Vlc.connectMainOuts(24);
		});
		if(config == \nime2015, {
			"jack_connect jacktrip:receive_1 sclang:in1".systemCmd;
			"jack_connect jacktrip:receive_2 sclang:in2".systemCmd;
			"jack_connect sclang:out33 jacktrip:send_1".systemCmd;
			"jack_connect sclang:out34 jacktrip:send_2".systemCmd;
			Vlc.connectMainOuts(32);
		});
	}

	*connectMainOuts {
		|n| n.do {
			|x|
			var cmd = "jack_connect sclang:out" ++ ((x+1).asString) ++ " jacktrip:send_" ++ ((x+1).asString);
			cmd.postln;
			cmd.systemCmd;
		}
	}

	*test {
		Pbindef(\test,\dur,0.25);
		Pbindef(\test,\midinote,62);
		Pbindef(\test,\out,Pdefn(\nomain));
		Pbindef(\test).play;
		^Pdef(\test);
	}

	*record {
		recBuffer = Buffer.alloc(Server.default,65536,4);
		recBuffer.write(("~/verylongcat"++(Date.getDate.stamp)++".wav").standardizePath,"WAV","int24",0,0,true);
		recSynth = SynthDef(\recSynth,{
			arg bufnum;
			var tablaL = SoundIn.ar(0);
			var tablaR = SoundIn.ar(1);
			var noTablaL = DelayN.ar(In.ar(32),1.0,~latency.kr);
			var noTablaR = DelayN.ar(In.ar(33),1.0,~latency.kr);
			DiskOut.ar(bufnum,[tablaL,tablaR,noTablaL,noTablaR]*(-10.dbamp));
		}).play(outputGroup,[bufnum:recBuffer.bufnum],addAction: 'addToTail');
	}

	*stopRecording {
		recSynth.free;
		recBuffer.close;
		recBuffer.free;
	}

	*to { |x| ^mainBus.index+x; }

	*tree { RootNode(Server.default).queryTree; }

}


+ NodeProxy {

	to {
		| where |
		this.playN(Vlc.mainBus.index+where);
		^this;
	}

}
