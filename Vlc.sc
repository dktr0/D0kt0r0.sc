Vlc {

	classvar <config; // symbol naming current configuration
	classvar <snova;
	classvar <latency;
	classvar <speakers; // dictionary of speaker sets
	classvar <nchnls,<leftChannels,<rightChannels; // for stereo mixdowns
	classvar <proxySpace;
	classvar <jitGroup,<mainGroup,<outputGroup;
	classvar <mainBus;
	classvar <delayedSynth,<noTablaSynth,<tablaSynth;
	classvar recSynth,recBuffer;

	*meow {
		| cfg=\stereo, jack=true, supernova=true |
		config = cfg;
		snova = supernova;
		latency = 0.135;
		if(supernova==true,{Server.supernova});
		Server.default = Server.local;
		if(jack==true,{Server.default.options.device = "JackRouter"});
		Server.default.options.numOutputBusChannels = 34;
		Server.default.options.numInputBusChannels = 2;
		Server.default.options.numAudioBusChannels = 256;
		proxySpace = ProxySpace.new.push;
		proxySpace.fadeTime = 2;
		Server.default.waitForBoot( { Vlc.afterBoot; "meow".postln; });
	}

	*afterBoot {
		mainBus = Bus.audio(Server.default,32);
		jitGroup = Group.head;
		mainGroup = ParGroup.tail;
		outputGroup = ParGroup.tail;
		proxySpace.group = jitGroup;
		Vlc.spaces;
		Vlc.nodes;
		Vlc.synths;
		Vlc.outputs;
		Vlc.jack;
		Vlc.record;
	}

	*spaces {
		if(config == \liveLab,{
			speakers = Dictionary[
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
				\down -> ((1..12)-1)
			];
			nchnls=24;
			leftChannels = [ ]; // *** missing ***
			rightChannels = [ ]; // *** missing ***
		});
		if(config == \stereo,{
			speakers = Dictionary[
				\left -> [0],
				\right -> [1],
				\all -> [0,1],
			];
			nchnls=2;
			leftChannels = [0];
			rightChannels = [1];
		});
		if(config == \nime2015, {
			speakers = Dictionary[
				\stage -> ((1..8)-1),
				\left -> ((9..12)-1),
				\right -> ((13..16)-1),
				\back -> ((17..24)-1),
				\up -> ((25..32)-1),
				\all -> ((1..32)-1)
			];
			nchnls=32;
			leftChannels = ([1,3,5,7]++(9..12)++(17..20)++(25..28))-1+mainBus.index;
			rightChannels = ([2,4,6,8]++(13..16)++(21..24)++(29..32))-1+mainBus.index;

		});

		speakers.keysValuesDo( { | key,value | Pdefn(key,Pxrand(value+mainBus.index,inf)); });
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
		thisProcess.interpreter.executeFile("~/d0kt0r0.sc/synths.scd".standardizePath);
	}

	*outputs {
		Vlc.playDelayedSynth;
		Vlc.playNoTablaSynth;
		Vlc.playTablaSynth;
	}

	*playDelayedSynth {
		if(delayedSynth.notNil,{delayedSynth.free});
		delayedSynth = SynthDef(\delayedSynth,{
			Out.ar(0,DelayN.ar(In.ar(mainBus.index,nchnls),0.5,~latency.kr,-6.dbamp));
		}).play(target:outputGroup);
	}

	*playTablaSynth {
		if(tablaSynth.notNil,{tablaSynth.free});
		tablaSynth = SynthDef(\tablaSynth,{
			var env = Lag.ar(K2A.ar(~tabla.kr.dbamp),lagTime:4);
			Out.ar(0,[SoundIn.ar(0),SoundIn.ar(1)]*env);
		}).play(target:outputGroup);
	}

	*playNoTablaSynth {
		noTablaSynth = SynthDef(\noTablaSynth,{
			var left = Mix.new(In.ar(leftChannels));
			var right = Mix.new(In.ar(rightChannels));
			Out.ar(32,[left,right]/2);
		}).play(target:outputGroup);
	}

	*jack {
		Vlc.connectJackTrip;
		Vlc.connectMainOuts(nchnls);
	}

	*connectJackTrip {
		var serverName = if(snova==true,"supernova","sclang");
		("/usr/local/bin/jack_connect jacktrip:receive_1 "++serverName++":in1").postln.systemCmd;
		("/usr/local/bin/jack_connect jacktrip:receive_2 "++serverName++":in2").postln.systemCmd;
		("/usr/local/bin/jack_connect "++serverName++":out33 jacktrip:send_1").postln.systemCmd;
		("/usr/local/bin/jack_connect "++serverName++":out33 jacktrip:send_1").postln.systemCmd;
	}

	*connectMainOuts {
		|n|
		var serverName = if(snova==true,"supernova","sclang");
		n.do {
			|x|
			var cmd = "/usr/local/bin/jack_connect "++serverName++":out" ++ ((x+1).asString) ++ " system:playback_" ++ ((x+1).asString);
			cmd.postln;
			cmd.systemCmd;
		}
	}

	*test {
		Pbindef(\test,\dur,0.25);
		Pbindef(\test,\midinote,62);
		Pbindef(\test,\out,Pdefn(\all));
		Pbindef(\test,\instrument,\point);
		Pbindef(\test,\group,mainGroup);
		Pbindef(\test).play;
		^Pdef(\test);
	}

	*record {
		if(recSynth.isNil,{
			recBuffer = Buffer.alloc(Server.default,65536,4);
			recBuffer.write(("~/verylongcat"++(Date.getDate.stamp)++".wav").standardizePath,"WAV","int24",0,0,true);
			recSynth = SynthDef(\recSynth,{
				arg bufnum;
				var tablaL = SoundIn.ar(0);
				var tablaR = SoundIn.ar(1);
				var noTablaL = Mix.new(In.ar(leftChannels));
				var noTablaR = Mix.new(In.ar(rightChannels));
				noTablaL = DelayN.ar(noTablaL,0.5,~latency.kr);
				noTablaR = DelayN.ar(noTablaR,0.5,~latency.kr);
				DiskOut.ar(bufnum,[tablaL,tablaR,noTablaL,noTablaR]*(-10.dbamp));
			}).play(outputGroup,[bufnum:recBuffer.bufnum],addAction: 'addToTail');
		},
		{
			"Warning: already recording".postln;
		});
	}

	*stopRecording {
		if(recSynth.notNil,{
			recSynth.free;
			recBuffer.close;
			recBuffer.free;
			recSynth = nil;
		},
		{
			"Warning: can't stop because not recording".postln;
		});
	}

	*to { |x| ^mainBus.index+x; }

	*tree { RootNode(Server.default).queryTree; }

	*quit {
		Vlc.stopRecording;
		Server.default.quit;
	}

	*latency_ { |x|
		latency=x;
		~latency = latency;
	}

	*delay {
		| signal,time |
		^DelayN.ar(signal,time-latency,time-~latency.kr);
	}

	*follow {
		| signal,time,db |
		^(DelayN.ar(~env.ar,time-latency,time-~latency.kr) *
			signal * (db.dbamp));
	}
}


+ NodeProxy {

	to {
		| where |
		this.playN(Vlc.mainBus.index+where);
		^this;
	}

}
