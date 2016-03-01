Vlc {

	classvar <config; // symbol naming current configuration
	classvar <usingJack;
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
	classvar <o;

	*devices {
		^ServerOptions.devices;
	}

	*meow {
		| cfg=\stereo, device="Fireface 400 (727)", supernova=false, sampleRate=48000, record=true |
		myAddress = "130.39.92.9";
		shawnsAddress = "132.206.162.199";
		qRange = 0.03;
		config = cfg;
		snova = supernova;
		// latency = 0.14;
		latency = 0;
		Server.default = Server.local;
		if(supernova,{Server.supernova},{Server.scsynth});
		if(device == "jack", {usingJack=true;Server.default.options.device = "JackRouter"},
			{
				usingJack=false;
				Server.default.options.device = device;
		});
		Server.default.options.sampleRate = sampleRate;
		Server.default.options.numOutputBusChannels = 2;
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
				if(usingJack,{Vlc.jack});
				if(cfg == \iclc2015,{Vlc.jackSystemInputs});
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
		o = mainBus.index;
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
		if(config == \iclc2015, {
			speakers = Dictionary[
				\front -> ([1,2]-1),
				\left -> ([1,3,5]-1),
				\right -> ([2,4,6]-1),
				\back -> ([7,8]-1),
				\all -> ((1..8)-1)
			];
			nchnls=8;
			leftChannels = [1,3,5,7]-1+mainBus.index;
			rightChannels = [2,4,6,8]-1+mainBus.index;
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
		~hall = 0;
		~noTabla = 5;
		//~noTablaThreshold = -10;
		//~noTablaRatio = 5;
		~tabla.fadeTime = 4;
		~tabla = -120;
		~tablaReverb = -120;
		~code = -10;
		~codeReverb = -120;
		~reverbRoom = 0.2;
		~reverbDamp = 0;
		~stereo = {SoundIn.ar([1,0])};
		~mono = {SoundIn.ar(0)+SoundIn.ar(1)};
		~env = {Amplitude.ar(SoundIn.ar(0)+SoundIn.ar(1),0.003,0.18)};
		~geF = 36.midicps;
		~giF = 48.midicps;
		~naF = 72.midicps;
		~na2F = 73.midicps;
		~tunF = 62.midicps;
		~tun2F = 63.midicps;
		~geQ = 40;
		~giQ = 40;
		~naQ = 400;
		~na2Q = 400;
		~tunQ = 12000;
		~tun2Q = 6400;
		~geG = 5;
		~giG = 7;
		~naG = 28;
		~na2G = 38;
		~tunG = -120;
		~tun2G = -120;
		~geA = { Resonz.ar(SoundIn.ar(0),~geF.kr,bwr:1/~geQ.kr,mul:~geG.kr.dbamp) };
		~giA = { Resonz.ar(SoundIn.ar(0),~giF.kr,bwr:1/~giQ.kr,mul:~giG.kr.dbamp) };
		~naA = { Resonz.ar(SoundIn.ar(1),~naF.kr,bwr:1/~naQ.kr,mul:~naG.kr.dbamp) };
		~na2A = { Resonz.ar(SoundIn.ar(1),~na2F.kr,bwr:1/~na2Q.kr,mul:~na2G.kr.dbamp) };
		~tunA = { Resonz.ar(SoundIn.ar(1),~tunF.kr,bwr:1/~tunQ.kr,mul:~tunG.kr.dbamp) };
		~tun2A = { Resonz.ar(SoundIn.ar(1),~tun2F.kr,bwr:1/~tun2Q.kr,mul:~tun2G.kr.dbamp) };
		~ge = { Amplitude.ar(~geA.ar,0.001,0.03) };
		~gi = { Amplitude.ar(~giA.ar,0.001,0.03) };
		~na = { Amplitude.ar(~naA.ar,0.001,0.03) };
		~na2 = { Amplitude.ar(~na2A.ar,0.001,0.03) };
		~tun = { Amplitude.ar(~tunA.ar,0.001,0.03) };
		~tun2 = { Amplitude.ar(~tun2A.ar,0.001,0.03) };
	}

	*synths {
		D0kt0r0.synths;
	}

	*outputs {
		// Vlc.playDelayedSynth;
		Vlc.playNoTablaSynth;
		// Vlc.playTablaSynth;
	}

	*playDelayedSynth {
		"playDelayedSynth".postln;
		if(delayedSynth.notNil,{delayedSynth.free});
		delayedSynth = SynthDef(\delayedSynth,{
			var env = Lag.ar(K2A.ar(~code.kr.dbamp),lagTime:1.5);
			var audio = In.ar(mainBus.index,nchnls);
			audio = DelayN.ar(audio,0.5,~latency.kr)*env;
			audio = audio * ~gain.kr.dbamp;
			audio = audio + FreeVerb.ar(audio,mix:1,
				room:Clip.kr(~reverbRoom.kr,0,1),
				damp:Clip.kr(~reverbDamp.kr,0,1),
				mul:~codeReverb.kr.dbamp);
			audio = Compander.ar(audio,audio,thresh:~threshold.kr.dbamp,slopeAbove:1/~ratio.kr,clampTime:0.002,relaxTime:0.1);
			Out.ar(0,audio);
		}).play(target:outputGroup);
	}

	*playTablaSynth {
		"playTablaSynth".postln;
		if(tablaSynth.notNil,{tablaSynth.free});
		tablaSynth = SynthDef(\tablaSynth,{
			var env,audio;
			// this is still not perfect - would be better if
			// ~tabla.kr.dbamp was at its value of -100 dB from beginning...
			env = Lag.ar(K2A.ar(~hall.kr.dbamp),lagTime:4);
			env = env * EnvGen.ar(Env.new([0,0,1],[4,4]));
			audio = Mix.new([SoundIn.ar(0),SoundIn.ar(1)])*env*0.5;
			//audio = audio + FreeVerb.ar(audio,mix:1,
			//	room:Clip.kr(~reverbRoom.kr,0,1),
			//	damp:Clip.kr(~reverbDamp.kr,0,1),
			//	mul:~tablaReverb.kr.dbamp);
			Out.ar(0,audio!2);
		}).play(target:outputGroup);
	}

	*playNoTablaSynth {
		"playNoTablaSynth".postln;
		if(noTablaSynth.notNil,{noTablaSynth.free});
		noTablaSynth = SynthDef(\noTablaSynth,{
			// var audio = [Mix.new(In.ar(leftChannels)),Mix.new(In.ar(rightChannels))];
			var audio = [In.ar(Vlc.mainBus.index),In.ar(Vlc.mainBus.index+1)];
			audio = audio * ~hall.kr.dbamp;
			audio = Compander.ar(audio,audio,thresh:~threshold.kr.dbamp,slopeAbove:1/~ratio.kr,clampTime:0.002,relaxTime:0.1);
			Out.ar(0,audio);
		}).play(target:outputGroup);
	}

	*jack {
		Vlc.connectJackTrip;
		if(config == \iclc2015,{Vlc.jackSystemInputs});
		Vlc.connectMainOuts(nchnls);
	}

	*jackSystemInputs {
		var serverName = if(snova==true,"supernova","scsynth");
		("/usr/local/bin/jack_connect system:capture_1 "++serverName++":in1").systemCmd;
		("/usr/local/bin/jack_connect system:capture_2 "++serverName++":in2").systemCmd;
	}

	*connectJackTrip {
		var serverName = if(snova==true,"supernova","scsynth");
		("/usr/local/bin/jack_connect shawn:receive_1 "++serverName++":in1").systemCmd;
		("/usr/local/bin/jack_connect shawn:receive_2 "++serverName++":in2").systemCmd;
		("/usr/local/bin/jack_connect "++serverName++":out33 shawn:send_1").systemCmd;
		("/usr/local/bin/jack_connect "++serverName++":out34 shawn:send_2").systemCmd;
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
			var nRecChannels = if(config==\iclc2015,10,4);
			recBuffer = Buffer.alloc(Server.default,65536,nRecChannels);
			recBuffer.write(("~/verylongcat"++(Date.getDate.stamp)++".wav").standardizePath,"WAV","int24",0,0,true);

			if(config!=\iclc2015, {
				recSynth = SynthDef(\recSynth,{
					arg bufnum;
					var tablaL = SoundIn.ar(0);
					var tablaR = SoundIn.ar(1);
					var noTablaL = Mix.new(In.ar(leftChannels))*(-10.dbamp);
					var noTablaR = Mix.new(In.ar(rightChannels))*(-10.dbamp);
					// noTablaL = DelayN.ar(noTablaL,0.5,~latency.kr);
					// noTablaR = DelayN.ar(noTablaR,0.5,~latency.kr);
					DiskOut.ar(bufnum,[tablaL,tablaR,noTablaL,noTablaR]);
				}).play(outputGroup,[bufnum:recBuffer.bufnum],addAction: 'addToTail');
			},{
				recSynth = SynthDef(\recSynth10,{
					arg bufnum;
					var tablaL = SoundIn.ar(0);
					var tablaR = SoundIn.ar(1);
					var octo = In.ar(mainBus.index,8);
					//var delayed = [sl,sr,left,right,bl,br,ul,ur].collect { |x|
					//	DelayN.ar(Mix.new(In.ar(x)),0.5,~latency.kr)*(-24.dbamp);
					//};
					DiskOut.ar(bufnum,[tablaL,tablaR]++octo);
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
		});
		if(x.isKindOf(Integer),{
			^(mainBus.index+x)!2;
		});
		if(x.isKindOf(Array),{
			if(x[0].isKindOf(Symbol),{
				^mainBus.index+x.collect({|item,index|speakers[item]});
			});
			if(x[0].isKindOf(Integer),{
				^mainBus.index+x;
			});
		});
		^nil;
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
		|x,n=8|
		var result = if(x.asArray.size>n,x.asArray.scramble,
			Array.fill(n,{|i|x.asArray.wrapAt(i)}).scramble);
		result = result + Array.fill(result.size,{|i|qRange.rand2});
		^result;
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
			^LFTri.ar(notes.kr.midicps,mul:db.dbamp);
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




