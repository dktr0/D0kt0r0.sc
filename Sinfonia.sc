Sinfonia {

	classvar <tempo;
	classvar <broadcast;

	classvar <inStereo;
	classvar <>piano;
	classvar <down,<up,<ceiling,<tweeters,<tablaOut,<voiceOut,<pianoOut;
	classvar <send;
	classvar <latency1;
	classvar <latency2;
	classvar <nchnls;
	classvar <mainBus,<proxySpace,<jitGroup,<mainGroup,<outputGroup;
	classvar mixSynth;
	classvar recSynth,recBuffer;

	*initClass {
		nchnls = 2;
		latency1 = 0.32;
		latency2 = 0.19;
		Sinfonia.broadcast_("192.168.0.255");
	}

	*boot { // for live coding soloist only
		|device,stereo=false|
		inStereo = stereo;
		if(device.notNil,{
			if(device == "nil",{
				Server.local.options.device = nil;
			},{
				Server.local.options.device = device;
			});
		},{
			Server.local.options.device = "JackRouter";
		});
		Server.local.options.sampleRate = 48000;
		Server.local.options.numInputBusChannels = 8;
		Server.local.options.numOutputBusChannels = 40;
		Server.local.options.numAudioBusChannels = 256;
		Server.local.options.memSize = 8192*64;
		Server.default = Server.local;
		Server.local.waitForBoot( { Sinfonia.afterBoot });
	}

	*afterBoot { // for live coding soloist only
		Sinfonia.jackConnections;
		mainBus = Bus.audio(Server.default,nchnls);
		jitGroup = Group.head;
		mainGroup = ParGroup.tail;
		outputGroup = ParGroup.tail;
		Sinfonia.nodes;
		Sinfonia.spaces;
		Sinfonia.mixSynth;
		Sinfonia.firstSynth;
		Sinfonia.bootMidi;
		D0kt0r0.synths;
	}

	*jackConnections {
		var sc = "scsynth",numberOutputs=36;
		// connect inputs to SuperCollider
		("/usr/local/bin/jack_connect shawn:receive_1 "++sc++":in1").systemCmd;
		("/usr/local/bin/jack_connect shawn:receive_2 "++sc++":in2").systemCmd;
		("/usr/local/bin/jack_connect kristin:receive_1 "++sc++":in3").systemCmd;
		("/usr/local/bin/jack_connect REAPER:out1 "++sc++":in4").systemCmd;
		("/usr/local/bin/jack_connect REAPER:out2 "++sc++":in5").systemCmd;
		// connect SuperCollider output mixes to jacktrip
		("/usr/local/bin/jack_connect "++sc++":out"++((numberOutputs+1).asString)++" shawn:send_1").systemCmd;
		("/usr/local/bin/jack_connect "++sc++":out"++((numberOutputs+2).asString)++" shawn:send_2").systemCmd;
		("/usr/local/bin/jack_connect "++sc++":out"++((numberOutputs+3).asString)++" kristin:send_1").systemCmd;
		("/usr/local/bin/jack_connect "++sc++":out"++((numberOutputs+3).asString)++" kristin:send_2").systemCmd;
		// connect SuperCollider outputs to system outputs
		numberOutputs.do( { |n|
			("/usr/local/bin/jack_connect "++sc++":out"++((n+1).asString)++" system:playback_"++((n+1).asString)).systemCmd;
		});
	}

	*nodes {
		Ndef(\latency1,latency1);
		Ndef(\latency2,latency2);
		Ndef(\code,-10).fadeTime=2;
		Ndef(\tabla,-10).fadeTime=2;
		Ndef(\tablaMonitor,6).fadeTime=2;
		Ndef(\voice,-10).fadeTime=2;
		Ndef(\voiceMonitor,6).fadeTime=2;
		Ndef(\piano,14).fadeTime=2;
		Ndef(\hall,3).fadeTime=2;
		Ndef(\threshold,-3).fadeTime=2;
		Ndef(\ratio,10).fadeTime=2;
	}

	*spaces {
		down = [1,2,4,6,8,7,5,3]-1+Sinfonia.mainBus.index;
		up = [9,10,12,14,16,15,13,11]-1+Sinfonia.mainBus.index;
		ceiling = [17,18,20,19]-1+Sinfonia.mainBus.index;
		tweeters = [21,22,24,23,26,28,27,25]-1+Sinfonia.mainBus.index;
		tablaOut = [29,30]-1+Sinfonia.mainBus.index;
		voiceOut = [9,10]-1+Sinfonia.mainBus.index;
		pianoOut = [1,2]-1+Sinfonia.mainBus.index;
		Pdefn(\down,Pxrand(down,inf));
		Pdefn(\up,Pxrand(down,inf));
		Pdefn(\ceiling,Pxrand(down,inf));
		Pdefn(\tweeters,Pxrand(down,inf));
	}

	*bootMidi {
		MIDIClient.init;
		piano = MIDIOut.newByName("IAC Driver", "Bus 1");
	}

	*broadcast_ {
		|x|
		broadcast = x;
		NetAddr.broadcastFlag = true;
		send = NetAddr(broadcast,NetAddr.langPort);
	}

	*chord {
		| notes |
		send.sendMsg("/sinfonia/chord",notes.asCompileString);
		topEnvironment.put(\chord,notes);
	}

	*firstSynth {
		SynthDef(\melody,{}).add;
	}

	*pser {
		| array,n |
		^Pseq([Pser(array,n),Pseq([array.last],inf)]);
	}

	*firstMaterial {
		| n |
		var array,nn,tempo,dur,seq;
		if(n == 0, {
			array = [71];
			nn = 8;
			tempo = 60;
			dur = 16;
		});
		if(n == 1, {
			array = [71,67,64,72,69,66,63,71];
			nn = 32;
			tempo = 72;
			dur = 4;
		});
		if(n == 2, {
			array = [71,67,64,72,69,66,63,71,76,64,67,71,74,62,65,69];
			nn = 128;
			tempo = 84;
			dur = 1;
		});
		if(n == 3, {
			array = [71,70,71,67,67,66,67,64,76,71,67,64];
			nn = 512;
			tempo = 96;
			dur = 0.25;
		});
		if(n == 4, {
			array = [79,78,76,71,67,66,64,71,67,64,59];
			nn = 512;
			tempo = 96;
			dur = 0.25;
		});
		if(n == 4, {
			array = [71,67,64,72,69,66,63,71,76,64,67,71,74,62,65,69];
			nn = 240;
			tempo = 52;
			dur = 2;
			Tdef(\firstAccel,{
				TempoClock.tempo = 52/60; 2.wait;
				Pdefn(\dur,2);
				16.do { TempoClock.tempo = TempoClock.tempo * 1.02; 2.wait; };
				Pdefn(\dur,1);
				16.do { TempoClock.tempo = TempoClock.tempo * 1.02; 2.wait; };
				Pdefn(\dur,0.5);
				16.do { TempoClock.tempo = TempoClock.tempo * 1.02; 2.wait; };
				Pdefn(\dur,0.25);
				16.do { TempoClock.tempo = TempoClock.tempo * 1.02; 2.wait; };
			}).play(quant:1);
		});
		seq = Sinfonia.pser(array,nn);
		Pdefn(\midinote,seq);
		Pdefn(\dur,dur);
		TempoClock.tempo = tempo/60;
		^array;
	}

	*firstMovement {
		// accelerating, with a fixed pitch cycle (each pitch used once)
		// a. from long string-like tones to pointy flourishes
		// b. tabla joins near end of movement, call and response
		// c. interrupted; final flourish reduces down to low E (various timbres)
		// Sinfonia.record;
		Sinfonia.firstSynth;
		Sinfonia.firstMaterial(0);
		Pdefn(\instrument,\melody);
		Pdef(\first,Pbind(
			\instrument,Pdefn(\instrument),
			\dur,Pdefn(\dur),
			\midinote,Pdefn(\midinote)
		)).play(quant:1);
	}

	*a { Sinfonia.firstMaterial(1); }
	*b { Sinfonia.firstMaterial(2); }
	*c { Sinfonia.firstMaterial(3); }
	*d { Sinfonia.firstMaterial(4); }

	*secondMovement {
		TempoClock.tempo = 72/60;
		~chord = [ 60, 62, 64, 65, 67 ];
		Sinfonia.signalToRecord;
		Tdef(\two,{
			TempoClock.tempo = 1;
			Sinfonia.chord([60,62,64,65,67]); 16.wait;
			14.do {
				Sinfonia.chord([64,66,68,69,71]); 4.wait;
				Sinfonia.chord([59,60,62,64,66]); 4.wait;
				Sinfonia.chord([64,66,68,69,71]); 4.wait;
				Sinfonia.chord([59,60,62,64,66]); 4.wait;
				Sinfonia.chord([64,66,68,69,71]); 4.wait;
				Sinfonia.chord([59,61,63,64,66]); 4.wait;
				Sinfonia.chord([57,61,64,66,68]); 4.wait;
				Sinfonia.chord([60,62,64,65,67]); 4.wait;
			};
			Sinfonia.chord([64,59]);
		}).play;
		Pdef(\piano,Pbind(\type,\midi,\midiout,Sinfonia.piano,\dur,8));
		Pdef(\ghost,Pbind(\midinote,Pfunc({~chord.choose})) <> Pdef(\piano));
		Pdef(\ghost).play;
	}

	*thirdMaterial {
		|n|
		if(n==0,{
			Pdef(\rhythm,Pbind(\dur,Pseq([4],inf)));
			Pdef(\rhythm).quant_(4);
		});
		if(n==1,{
			Pdef(\rhythm).quant_(4);
			Pdef(\rhythm,Pbind(\dur,Pseq([1,3],inf)));
		});
		if(n==2,{
			Pdef(\rhythm).quant_(4);
			Pdef(\rhythm,Pbind(\dur,Pseq([1,1,2],inf)));
		});
		if(n==3,{
			Pdef(\rhythm).quant_(4);
			Pdef(\rhythm,Pbind(\dur,Pseq([1,1,1.25,0.75],inf)));
		});
		if(n==4,{
			Pdef(\rhythm).quant_(4);
			Pdef(\x,Pbind(\dur,Pseq([1,0.5,0.5,1.25,0.75],inf)));
		});
		if(n==5,{
			Pdef(\rhythm).quant_(4);
			Pdef(\x,Pbind(\dur,Pseq([0.5,0.5,0.5,0.5,1.25,0.75],inf)));
		});
		if(n==6,{
			Pdef(\rhythm).quant_(4);
			Pdef(\rhythm,Pbind(\dur,Pseq([0.5,0.5,0.5,0.5,1.25,0.5,0.25],inf)));
		});
		if(n==7,{
			Pdef(\rhythm).quant_(4);
			Pdef(\rhythm,Pbind(\dur,Pseq([0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,1.25,0.25,0.25,0.25],inf)));
		});
		if(n==8,{
			Pdef(\rhythm).quant_(4);
			Pdef(\rhythm,Pbind(\dur,Pseq([1,2,1],inf)));
		});
		if(n==9,{
			Pdef(\rhythm).quant_(4);
			Pdef(\rhythm,Pbind(\dur,Pseq([3,1],inf)));
		});
		if(n==10,{
			Pdef(\rhythm).quant_(4);
			Pdef(\rhythm,Pbind(\dur,Pseq([4],inf)));
		});
		if(n==11,{
			Pdef(\rhythm).quant_(4);
			Pdef(\rhythm,Pbind(\dur,Pseq([Rest(4)],inf)));
		});
	}

	*thirdMovement {
		Sinfonia.signalToRecord;
		[96,95,93,91,88,90,86].postln;
	}

	*longDelay {
		var dt = 60/112*28-latency1-latency2;
		Ndef(\delay1,{DelayN.ar(SoundIn.ar(2),dt,dt,0.dbamp)*Ndef(\voice).kr});
		Ndef(\delay1).play(Sinfonia.mainBus.index+[11,15,14]-1,group:mainGroup);
		Ndef(\delay2,{DelayN.ar(Ndef(\delay1).ar,dt,dt,0.dbamp)*Ndef(\voice).kr});
		Ndef(\delay2).play(Sinfonia.mainBus.index+[13,35,34]-1,group:mainGroup);
		Ndef(\delay3,{DelayN.ar(Ndef(\delay2).ar,dt,dt,0.dbamp)*Ndef(\voice).kr});
		Ndef(\delay3).play(Sinfonia.mainBus.index+[36,16,12]-1,group:mainGroup);
	}

	*longDelayStop {
		Ndef(\delay1).stop(45);
		Ndef(\delay2).stop(45);
		Ndef(\delay3).stop(45);
	}

	*infinite {
		var dt = 60/112*8-latency1-latency2;
		Ndef(\infinite,0).fadeTime=4;
		Ndef(\infinite1,{DelayN.ar(SoundIn.ar(2),dt,dt,-1.dbamp)*Ndef(\infinite).kr.dbamp});
		Ndef(\infinite2,{DelayN.ar(Ndef(\infinite1).ar,dt,dt,-1.dbamp)*Ndef(\infinite).kr.dbamp});
		Ndef(\infinite3,{DelayN.ar(Ndef(\infinite2).ar,dt,dt,-1.dbamp)*Ndef(\infinite).kr.dbamp});
		Ndef(\infinite4,{DelayN.ar(Ndef(\infinite3).ar,dt,dt,-1.dbamp)*Ndef(\infinite).kr.dbamp});
		Ndef(\infinite5,{DelayN.ar(Ndef(\infinite4).ar,dt,dt,-1.dbamp)*Ndef(\infinite).kr.dbamp});
		Ndef(\infinite6,{DelayN.ar(Ndef(\infinite5).ar,dt,dt,-1.dbamp)*Ndef(\infinite).kr.dbamp});
		Ndef(\infinite7,{DelayN.ar(Ndef(\infinite6).ar,dt,dt,-1.dbamp)*Ndef(\infinite).kr.dbamp});
		Ndef(\infinite1).play(Sinfonia.mainBus.index+32,group:mainGroup);
		Ndef(\infinite2).play(Sinfonia.mainBus.index+8,group:mainGroup);
		Ndef(\infinite3).play(Sinfonia.mainBus.index+1,group:mainGroup);
		Ndef(\infinite4).play(Sinfonia.mainBus.index+0,group:mainGroup);
		Ndef(\infinite5).play(Sinfonia.mainBus.index+9,group:mainGroup);
		Ndef(\infinite6).play(Sinfonia.mainBus.index+16,group:mainGroup);
		Ndef(\infinite7).play(Sinfonia.mainBus.index+17,group:mainGroup);
	}

	*infiniteStop {
		|t=45|
		Ndef(\infinite1).stop(t);
		Ndef(\infinite2).stop(t);
		Ndef(\infinite3).stop(t);
		Ndef(\infinite4).stop(t);
		Ndef(\infinite5).stop(t);
		Ndef(\infinite6).stop(t);
		Ndef(\infinite7).stop(t);
	}

	*fourthMovement {
		TempoClock.tempo = 112/60;
		Ndef(\tablaMonitor,-3);
		Ndef(\voiceMonitor,-3);
		Sinfonia.signalToRecord;
		Tdef(\fourthMovement, {
			// k is 196 beats (7 times 7 bars times 4 beats)
			"1st cycle of 7".postln;
			Pbindef(\passacaglia,
				\instrument,\chebyBass,
				\midinote,Pseq([24],1),
				\fnoise,0.01,
				\dur,28,
				\legato,1.05,
				\preamp,1,
				\out,Sinfonia.down).play(quant:4);
			Pbindef(\passacaglia).quant = 4;
			28.wait; "2nd cycle of 7".postln;
			Pbindef(\passacaglia,
				\midinote,Pseq([24,26,30,28,31,26,23],inf),
				\dur,4,\legato,1.05);
			Sinfonia.thirdMaterial(0); 28.wait; "2nd cycle of 7".postln;
			Pbindef(\passacaglia,\preamp,0.03.dbamp);
			Sinfonia.thirdMaterial(1); 28.wait; "3rd cycle of 7".postln;
			Sinfonia.thirdMaterial(2); 28.wait; "4th cycle of 7".postln;
			Pbindef(\passacaglia,\preamp,0.06.dbamp);
			Sinfonia.thirdMaterial(3); 28.wait; "5th cycle of 7".postln;
			Sinfonia.thirdMaterial(4); 28.wait; "6th cycle of 7".postln;
			Pbindef(\passacaglia,\preamp,0.09.dbamp);
			Sinfonia.thirdMaterial(5); 28.wait; "7th cycle of 7".postln;

			// l: voice enters
			Sinfonia.longDelay;
			Sinfonia.thirdMaterial(6);
			"1st cycle of 7".postln; 28.wait;
			Pbindef(\passacaglia,\preamp,0.1.dbamp);
			"2nd cycle of 7".postln; 28.wait;
			Pbindef(\passacaglia,\preamp,0.2.dbamp);
			"3rd cycle of 7".postln; 28.wait;
			Pbindef(\passacaglia,\preamp,0.3.dbamp);
			"4th cycle of 7".postln; 28.wait;
			Pbindef(\passacaglia,\preamp,0.4.dbamp);
			"5th cycle of 7".postln; 28.wait;
			Pbindef(\passacaglia,\preamp,0.5.dbamp);
			"6th cycle of 7".postln; 28.wait;
			Pbindef(\passacaglia,\preamp,0.8.dbamp);
			"7th cycle of 7".postln; 28.wait;
			Pbindef(\passacaglia,\preamp,1.0.dbamp);

			// m: delay of voice decays, beats are most intense
			Sinfonia.thirdMaterial(7);
			"1st cycle of 7".postln; 28.wait;
			"2nd cycle of 7".postln; 28.wait;
			"3rd cycle of 7".postln; 28.wait;
			"4th cycle of 7".postln; 28.wait;
			Sinfonia.longDelayStop;
			"5th cycle of 7".postln; 28.wait;
			"6th cycle of 7".postln; 28.wait;
			"7th cycle of 7".postln; 28.wait;

			// n:
			"section n".postln;
			Sinfonia.infinite;
			Pbindef(\passacaglia,\preamp,0.5.dbamp);
			Pbindef(\passacaglia,\lpf,5000);
			Sinfonia.thirdMaterial(8); 84.wait;
			Pbindef(\passacaglia,\lpf,2500);
			Sinfonia.thirdMaterial(9); 56.wait;
			Pbindef(\passacaglia,\lpf,1250);
			Sinfonia.thirdMaterial(10); 56.wait;
			Pbindef(\passacaglia,\lpf,600);
			Sinfonia.thirdMaterial(11);
		}).play(quant:1);
	}

	*testSendToShawn {
		^Pdef(\test,Pbind(
			\instrument,\zip,
			\dur,0.25,
			\out,Pseq([0,1],inf)+nchnls,
		)).play;
	}

	*testSendToKristin {
		^Pdef(\test,Pbind(
			\instrument,\zip,
			\dur,0.25,
			\out,nchnls+2,
		)).play;
	}

	*testPiano {
		^Pdef(\test,Pbind(
			\type,\midi,
			\midiout,Sinfonia.piano,
			\dur,0.25
		)).play;
	}

	*testSpeakers {
		|x|
		^Pdef(\test,Pbind(
			\instrument,\zip,
			\dur,0.25,
			\out,Pseq(x,inf)
		)).play;
	}

	*mixSynth {
		var ml = 0.6;
		if(mixSynth.notNil,{mixSynth.free});
		mixSynth = SynthDef(\mix,{
			var codeMulti = In.ar(mainBus.index,nchnls) * Sinfonia.levelGen(Ndef(\code));
			var codeStereo = Mix.new(codeMulti)!2*0.5;
			var codeMono = Mix.new(codeMulti);
			var pianoStereo = [SoundIn.ar(3),SoundIn.ar(4)] * Sinfonia.levelGen(Ndef(\piano));
			var pianoMono = Mix.new(pianoStereo)*0.5;
			var shawn = Mix.new([SoundIn.ar(0),SoundIn.ar(1)]) * 0.5 * Sinfonia.levelGen(Ndef(\tabla));
			var kristin = [SoundIn.ar(2) * Sinfonia.levelGen(Ndef(\voice))]*0.5;
			var shawnDelayed,codeDelayed,mix,limit,shawnPlaced,kristinPlaced,pianoDelayed,pianoPlaced;

			// mix for shawn
			mix = (codeStereo + pianoStereo + [kristin,kristin]) * Sinfonia.levelGen(Ndef(\tablaMonitor));
			limit = Compander.ar(mix,mix,thresh:Ndef(\threshold).kr.dbamp,slopeAbove:1/Ndef(\ratio).kr,clampTime:0.002,relaxTime:0.1);
			Out.ar(nchnls,limit!2);

			// mix for kristin
			codeDelayed = DelayN.ar(codeMono+pianoMono,ml,Ndef(\latency1).kr);
			mix = (codeDelayed + shawn) * Sinfonia.levelGen(Ndef(\voiceMonitor));
			limit = Compander.ar(mix,mix,thresh:Ndef(\threshold).kr.dbamp,slopeAbove:1/Ndef(\ratio).kr,clampTime:0.002,relaxTime:0.1);
			Out.ar(nchnls+2,limit!2);

			// mix for hall
			if(inStereo,{
				shawnPlaced = DelayN.ar(shawn,ml,Ndef(\latency2).kr)!2;
				kristinPlaced = kristin!2;
				pianoPlaced = DelayN.ar(pianoStereo,ml,Ndef(\latency1).kr+Ndef(\latency2).kr);
				codeDelayed = DelayN.ar(codeStereo,ml,Ndef(\latency1).kr+Ndef(\latency2).kr);
			},{
				shawnDelayed = DelayN.ar(shawn,ml,Ndef(\latency2).kr);
				shawnPlaced = (0!28) ++ (shawnDelayed!2);
				kristinPlaced = [0,0,0,0,0,0,0,0,kristin,kristin,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,kristin];
				pianoDelayed = DelayN.ar(pianoStereo,ml,Ndef(\latency1).kr+Ndef(\latency2).kr);
				pianoPlaced = [pianoDelayed[0],pianoDelayed[1]] ++ (0!30) ++ [(pianoDelayed[0]+pianoDelayed[1])*0.5];
				codeDelayed = DelayN.ar(codeMulti,ml,Ndef(\latency1).kr+Ndef(\latency2).kr);
			});
			mix = (kristinPlaced + shawnPlaced + codeDelayed + pianoPlaced) * Sinfonia.levelGen(Ndef(\hall));
			limit = Compander.ar(mix,mix,thresh:Ndef(\threshold).kr.dbamp,slopeAbove:1/Ndef(\ratio).kr,clampTime:0.002,relaxTime:0.1);
			Out.ar(0,limit);

		}).play(target:outputGroup);
	}

	*record {
		if(recSynth.notNil,{"Warning: already recording".postln;},{
			var nRecChannels = 10;
			recBuffer = Buffer.alloc(Server.default,65536,nRecChannels);
			recBuffer.write(("~/sinfonia-"++(Date.getDate.stamp)++".wav").standardizePath,"WAV","int24",0,0,true);
			recSynth = SynthDef(\recSynth,{
				arg bufnum;
				var tablaL = SoundIn.ar(0);
				var tablaR = SoundIn.ar(1);
				var voice = SoundIn.ar(2);
				var pianoL = SoundIn.ar(3);
				var pianoR = SoundIn.ar(4);
				var leftLow = Mix.new(In.ar([1,3,36,5,7]+Sinfonia.mainBus.index))*0.1;
				var rightLow = Mix.new(In.ar([2,4,35,6,8]+Sinfonia.mainBus.index))*0.1;
				var leftHigh = Mix.new(In.ar([9,11,13,15,21,23,25,27,17,19]+Sinfonia.mainBus.index))*0.1;
				var rightHigh = Mix.new(In.ar([10,12,14,16,22,24,26,28,18,20]+Sinfonia.mainBus.index))*0.1;
				var centre = Mix.new(In.ar([33,35]+Sinfonia.mainBus.index))*0.1;
				DiskOut.ar(bufnum,[tablaL,tablaR,voice,pianoL,pianoR,leftLow,rightLow,leftHigh,rightHigh,centre]);
			}).play(outputGroup,[bufnum:recBuffer.bufnum],addAction: 'addToTail');
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


	// utility functions:

	*levelGen {
		|x|
		^(Lag.ar(K2A.ar(x.kr.dbamp),lagTime:2)*EnvGen.ar(Env.new([0,0,1],[2,2])));
	}

	*latency1_ {
		|x|
		latency1 = x;
		~latency1 = x;
	}

	*latency2_ {
		|x|
		latency2 = x;
		~latency2 = x;
	}

	*out {
		|choices,audio|
		^Out.ar(Select.kr(IRand(0,choices.size-1),choices),audio);
	}
}
