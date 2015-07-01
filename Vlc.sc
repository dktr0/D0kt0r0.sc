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
	classvar <>qRange;
	classvar <>myAddress,<>shawnsAddress;

	*meow {
		| cfg=\stereo, jack=true, supernova=true, sampleRate=48000, record=true |
		myAddress = "130.39.92.9";
		shawnsAddress = "132.206.162.199";
		qRange = 0.03;
		config = cfg;
		snova = supernova;
		latency = 0.14;
		Server.default = Server.local;
		if(supernova,{Server.supernova},{Server.scsynth});
		if(jack,{Server.default.options.device = "JackRouter"},
			{Server.default.options.device = nil});
		Server.default.options.sampleRate = sampleRate;
		Server.default.options.numOutputBusChannels = 34;
		Server.default.options.numInputBusChannels = 2;
		Server.default.options.numAudioBusChannels = 256;
		if(Server.default.serverRunning,{
			Vlc.stopRecording; // a second meow restarts recording and resets key resources with no reboot
			fork {
				2.wait;
				Vlc.spaces;
				Vlc.nodes;
				Vlc.synths;
				Vlc.outputs;
				if(jack,{Vlc.jack});
				if(record,{Vlc.record});
				"meow meow meow".postln;
			};
		},{
			proxySpace = ProxySpace.new.push;
			proxySpace.fadeTime = 2;
			Server.default.waitForBoot( {
				Vlc.afterBoot;
				if(record,{Vlc.record});
				"meow".postln;
			});
		});
	}

	*meowMeow {
		if(recSynth.isNil.not,{Vlc.stopRecording});
		fork {
			2.wait;
			Vlc.record;
			"meow meow".postln;
		}
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
			leftChannels = [ ]+mainBus.index; // *** missing ***
			rightChannels = [ ]+mainBus.index; // *** missing ***
		});
		if(config == \stereo,{
			speakers = Dictionary[
				\left -> [0],
				\right -> [1],
				\all -> [0,1],
			];
			nchnls=2;
			leftChannels = [0]+mainBus.index;
			rightChannels = [1]+mainBus.index;
		});
		if(config == \nime2015, {
			speakers = Dictionary[
				\stage -> ((1..8)-1),
				\left -> ((9..12)-1),
				\right -> ((13..16)-1),
				\back -> ((17..24)-1),
				\up -> ((25..32)-1),
				\all -> ((1..32)-1),
				\noup -> ((1..24)-1),
				\hall -> ((9..32)-1)
			];
			nchnls=32;
			leftChannels = ([1,3,5,7]++(9..12)++(17..20)++(25..28))-1+mainBus.index;
			rightChannels = ([2,4,6,8]++(13..16)++(21..24)++(29..32))-1+mainBus.index;

		});
		"available spaces are:".postln;
		speakers.keysValuesDo( { | key,value |
			Pdefn(key,Pxrand(value+mainBus.index,inf));
			("  " ++ key).postln;
		});
	}

	*nodes {
		~test = { SinOsc.ar(440,mul:-20.dbamp) };
		~latency = latency;
		~gain = 0;
		~threshold = -3;
		~ratio = 10;
		~noTabla = 5;
		~noTablaThreshold = -10;
		~noTablaRatio = 5;
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
		~geG = 15;
		~giG = 27;
		~naG = 38;
		~tunG = 29;
		~geA = { Resonz.ar(SoundIn.ar(0),~geF.kr,bwr:1/~geQ.kr,mul:~geG.kr.dbamp) };
		~giA = { Resonz.ar(SoundIn.ar(0),~giF.kr,bwr:1/~giQ.kr,mul:~giG.kr.dbamp) };
		~naA = { Resonz.ar(SoundIn.ar(1),~naF.kr,bwr:1/~naQ.kr,mul:~naG.kr.dbamp) };
		~tunA = { Resonz.ar(SoundIn.ar(1),~tunF.kr,bwr:1/~tunQ.kr,mul:~tunG.kr.dbamp) };
		~ge = { Amplitude.ar(~geA.ar,0.001,0.03) };
		~gi = { Amplitude.ar(~giA.ar,0.001,0.03) };
		~na = { Amplitude.ar(~naA.ar,0.001,0.03) };
		~tun = { Amplitude.ar(~tunA.ar,0.001,0.03) };
	}

	*synths {
		/* SynthDef(\dly,{
			arg sustain=0.5, dly=0.25, amp=0.1, out=0;
			var audio,env;
			env = Env.linen(0.005,sustain-0.01,0.005);
			env = EnvGen.ar(env);
			audio = (SoundIn.ar(0)+SoundIn.ar(1))*env;
			audio = DelayN.ar(audio,10.0,dly-~latency.kr);
			env = EnvGen.ar(Env.linen(0.01,sustain+dly,0.01),doneAction:2);
			Out.ar(out,audio*env);
		}).add; */ // this is a bad idea because of all the memory allocation
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
			var audio = In.ar(mainBus.index,nchnls);
			audio = DelayN.ar(audio,0.5,~latency.kr);
			audio = audio * ~gain.kr.dbamp;
			audio = Compander.ar(audio,audio,thresh:~threshold.kr.dbamp,slopeAbove:1/~ratio.kr,clampTime:0.002,relaxTime:0.1);
			Out.ar(0,audio);
		}).play(target:outputGroup);
	}

	*playTablaSynth {
		if(tablaSynth.notNil,{tablaSynth.free});
		tablaSynth = SynthDef(\tablaSynth,{
			var env = Lag.ar(K2A.ar(~tabla.kr.dbamp),lagTime:4);
			var audio = Mix.new([SoundIn.ar(0),SoundIn.ar(1)])*env;
			Out.ar(0,audio!8);
		}).play(target:outputGroup);
	}

	*playNoTablaSynth {
		noTablaSynth = SynthDef(\noTablaSynth,{
			var audio = [Mix.new(In.ar(leftChannels)),Mix.new(In.ar(rightChannels))];
			audio = audio * ~noTabla.kr.dbamp;
			audio = Compander.ar(audio,audio,thresh:~noTablaThreshold.kr.dbamp,slopeAbove:1/~noTablaRatio.kr,clampTime:0.002,relaxTime:0.1);
			Out.ar(32,audio);
		}).play(target:outputGroup);
	}

	*jack {
		Vlc.connectJackTrip;
		Vlc.connectMainOuts(nchnls);
	}

	*connectJackTrip {
		var serverName = if(snova==true,"supernova","scsynth");
		("/usr/local/bin/jack_connect JackTrip:receive_1 "++serverName++":in1").systemCmd;
		("/usr/local/bin/jack_connect JackTrip:receive_2 "++serverName++":in2").systemCmd;
		("/usr/local/bin/jack_connect "++serverName++":out33 JackTrip:send_1").systemCmd;
		("/usr/local/bin/jack_connect "++serverName++":out34 JackTrip:send_2").systemCmd;
	}

	*connectMainOuts {
		|n|
		var serverName = if(snova==true,"supernova","scsynth");
		n.do {
			|x|
			var cmd = "/usr/local/bin/jack_connect "++serverName++":out" ++ ((x+1).asString) ++ " system:playback_" ++ ((x+1).asString);
			// cmd.postln;
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

	*tablaSendTest {
		Pbindef(\tablaSendTest,\dur,0.5);
		Pbindef(\tablaSendTest,\midinote,62);
		Pbindef(\tablaSendTest,\out,Pseq([32,33],inf));
		Pbindef(\tablaSendTest,\instrument,\point);
		Pbindef(\tablaSendTest,\group,mainGroup);
		Pbindef(\tablaSendTest).play;
		^Pdef(\tablaSendTest);
	}

	*record {
		if(recSynth.isNil,{
			var nRecChannels = if(config==\nime2015,10,4);
			recBuffer = Buffer.alloc(Server.default,65536,nRecChannels);
			recBuffer.write(("~/verylongcat"++(Date.getDate.stamp)++".wav").standardizePath,"WAV","int24",0,0,true);

			if(config!=\nime2015, {
				recSynth = SynthDef(\recSynth,{
					arg bufnum;
					var tablaL = SoundIn.ar(0);
					var tablaR = SoundIn.ar(1);
					var noTablaL = Mix.new(In.ar(leftChannels));
					var noTablaR = Mix.new(In.ar(rightChannels));
					noTablaL = DelayN.ar(noTablaL,0.5,~latency.kr)*(-10.dbamp);
					noTablaR = DelayN.ar(noTablaR,0.5,~latency.kr)*(-10.dbamp);
					DiskOut.ar(bufnum,[tablaL,tablaR,noTablaL,noTablaR]);
				}).play(outputGroup,[bufnum:recBuffer.bufnum],addAction: 'addToTail');
			},{
				recSynth = SynthDef(\recSynth10,{
					arg bufnum;
					var tablaL = SoundIn.ar(0);
					var tablaR = SoundIn.ar(1);
					var sl = [0,2,4,6]+mainBus.index;
					var sr = [1,3,5,7]+mainBus.index;
					var left = Vlc.speakers[\left]+mainBus.index;
					var right = Vlc.speakers[\right]+mainBus.index;
					var bl = Vlc.speakers[\back][0..3]+mainBus.index;
					var br = Vlc.speakers[\back][4..7]+mainBus.index;
					var ul = Vlc.speakers[\up][0..3]+mainBus.index;
					var ur = Vlc.speakers[\up][4..7]+mainBus.index;
					var delayed = [sl,sr,left,right,bl,br,ul,ur].collect { |x|
						DelayN.ar(Mix.new(In.ar(x)),0.5,~latency.kr)*(-24.dbamp);
					};
					DiskOut.ar(bufnum,[tablaL,tablaR]++delayed);
				}).play(outputGroup,[bufnum:recBuffer.bufnum],addAction: 'addToTail');
			});
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

	*to { |x|
		if(x.isKindOf(Symbol),{
			^mainBus.index+speakers[x];
		},{
			^mainBus.index+x;
		});
	}

	*tree { RootNode(Server.default).queryTree; }

	*quit {
		Vlc.stopRecording;
		fork {
			1.wait;
			Server.default.quit;
			"meow...".postln;
		}
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

	*q {
		|x|
		var base,dt;
		if(x.isArray,{
			if(x.size==1,{
				base = x[0]!4;
			});
			if(x.size==2,{
				base = x ++ x;
			});
			if(x.size==3,{
				base = x ++ [x[0]];
			});
			if(x.size==4,{
				base = x;
			});
		},{
			base = x!4;
		});
		dt = [qRange.rand2,qRange.rand2,qRange.rand2,qRange.rand2];
		^base + dt;
	}

	*saw {
		|notes=62,db=(-20)|
		if(notes.isKindOf(NodeProxy),{
			^Saw.ar(notes.kr.midicps,mul:db.dbamp);
		},{
			^Saw.ar(Vlc.q(notes).midicps,mul:db.dbamp);
		});
	}

	*sin {
		|notes=62,db=(-20)|
		if(notes.isKindOf(NodeProxy),{
			^SinOsc.ar(notes.kr.midicps,mul:db.dbamp);
		},{
			^SinOsc.ar(Vlc.q(notes).midicps,mul:db.dbamp);
		});
	}

	*tri {
		|notes=62,db=(-20)|
		if(notes.isKindOf(NodeProxy),{
			^LFTri.ar(Vlc.q(notes).midicps,mul:db.dbamp);
		},{
			^LFTri.ar(Vlc.q(notes).midicps,mul:db.dbamp);
		});
	}

	*tdef {
		|name,def|
		^Tdef(name, { inf.do(def) });
	}

	*penv {
		|dur=60,to=(-100),from=0|
		^(Pseq([0],inf)+Penv([from,to],[dur]));
	}

	*fade {
		|dur=60,to=(-100),from=0|
		^(Pseq([0],inf)+Penv([from,to],[dur]));
	}

}


+ NodeProxy {
	to {
		| where |
		this.playN(Vlc.to(where));
		^this;
	}
}




