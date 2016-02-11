Sinfonia {

	classvar <>allow;
	classvar <tempo;
	classvar <broadcast;
	classvar send;
	classvar <>piano;

	*initClass {
		allow = false;
		Sinfonia.broadcast_("255.255.255.255");
		Sinfonia.oscdefs;
		Sinfonia.tdefs;
	}

	*boot { // for live coding soloist only
		D0kt0r0.boot;
		D0kt0r0.synths;
		Sinfonia.firstSynth;
		Sinfonia.bootMidi;
	}

	*bootMidi {
		MIDIClient.init;
		piano = MIDIOut.newByName("IAC Driver", "Bus 1");
		Pdef(\piano,Pbind(\type,\midi,\midiout,Sinfonia.piano));
	}

	*start { // for orchestra members only
		allow = true;
	}

	*stop { // for orchestra members only
		allow = false;
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

	*tempo_ {
		|x|
		tempo = x;
		TempoClock.tempo = x;
		send.sendMsg("/sinfonia/tempo",x);
	}

	*event {
		if([0,0].choose == 0, {
			send.sendMsg("/sinfonia/orchestra")
		},{
			Tdef(\solo).play(quant:0);
		});
	}


	*oscdefs {
		OSCdef(\chord,
			{ |msg,time,addr,port|
				if(allow,{
					("/sinfonia/chord " ++ msg[1].asString).postln;
					topEnvironment.put(\chord,msg[1].asString.compile.value);
				});
		},"/sinfonia/chord").permanent_(true);
		OSCdef(\tempo,
			{ |msg,time,addr,port|
				if(allow,{
					("/sinfonia/tempo " ++ msg[1].asString).postln;
					TempoClock.tempo = msg[1];
				});
		},"/sinfonia/tempo").permanent_(true);
		OSCdef(\orchestra,
			{ |msg,time,addr,port|
				if(allow,{
					"/sinfonia/orchestra".postln;
					Tdef(\orchestra).play(quant:0);
				});
		},"/sinfonia/orchestra").permanent_(true);
	}

	*tdefs {

		Tdef(\two,{
			Sinfonia.tempo = 1;
			Sinfonia.chord([60,62,64,65,67]); 16.wait;
			8.do {
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
		});
		Tdef(\event, { inf.do {
			Sinfonia.event; ~period.wait; ~period.postln;
		}});
		Tdef(\four, { inf.do {
			Sinfonia.tempo = 90/60;
			~period = 1;
			Tdef(\event).play;
			Sinfonia.chord([64,67,71]); 3.wait;
			Sinfonia.chord([64,66,71]); 3.wait;
			Sinfonia.chord([63,67,72]); 3.wait;
			Sinfonia.chord([62,67,72]); 3.wait;
			Sinfonia.chord([61,67,69]); 3.wait;
			Sinfonia.chord([60,64,69]); 3.wait;
			Sinfonia.chord([59,63,67]); 3.wait;
			Sinfonia.chord([64,66,67]); 3.wait;
			~period = 0.5;
			Sinfonia.chord([64,67,71]); 3.wait;
			Sinfonia.chord([64,66,71]); 3.wait;
			Sinfonia.chord([63,67,72]); 3.wait;
			Sinfonia.chord([62,67,72]); 3.wait;
			Sinfonia.chord([61,67,69]); 3.wait;
			Sinfonia.chord([60,64,69]); 3.wait;
			Sinfonia.chord([59,63,67]); 3.wait;
			Sinfonia.chord([64,66,67]); 3.wait;
			~period = 0.25;
			Sinfonia.chord([64,67,71]); 3.wait;
			Sinfonia.chord([64,66,71]); 3.wait;
			Sinfonia.chord([63,67,72]); 3.wait;
			Sinfonia.chord([62,67,72]); 3.wait;
			Sinfonia.chord([61,67,69]); 3.wait;
			Sinfonia.chord([60,64,69]); 3.wait;
			Sinfonia.chord([59,63,67]); 3.wait;
			Sinfonia.chord([64,66,67]); 3.wait;
		}});
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
			array = [71,67,64,72];
			nn = 32;
			tempo = 72;
			dur = 4;
		});
		if(n == 2, {
			array = [71,67,64,72];
			nn = 128;
			tempo = 84;
			dur = 1;
		});
		if(n == 3, {
			array = [71,67,64,72];
			nn = 512;
			tempo = 96;
			dur = 0.25;
		});
		if(n == 4, {
			array = [71,67,64,72];
			nn = 512;
			tempo = 96;
			dur = 0.25;
		});
		if(n == 4, {
			array = [71,67,64,72];
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
				16+32+64+128
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
		Tdef(\four).stop;
		Tdef(\two).play;
// quiet and slow, with a fixed harmonic pattern
// e. ghost piano, alone, at first
// f. then laptop orchestra (powerbooks unplugged style) joins "pizzicato"
// g. live code adds swells
// h. then voice (vocalise)
// i. arrives at harmonic stasis (E major)
	}

	*thirdMaterial {
		|n|
		if(n==0,{
			Pdef(\x,Pbind(\dur,Pseq([4],inf)));
			Pdef(\x).quant_(4);
		});
		if(n==1,{
			Pdef(\x).quant_(4);
			Pdef(\x,Pbind(\dur,Pseq([1,3],inf)));
		});
		if(n==2,{
			Pdef(\x).quant_(4);
			Pdef(\x,Pbind(\dur,Pseq([1,1,2],inf)));
		});
		if(n==3,{
			Pdef(\x).quant_(4);
			Pdef(\x,Pbind(\dur,Pseq([1,1,1.25,0.75],inf)));
		});
		if(n==4,{
			Pdef(\x).quant_(4);
			Pdef(\x,Pbind(\dur,Pseq([1,0.5,0.5,1.25,0.75],inf)));
		});
		if(n==5,{
			Pdef(\x).quant_(4);
			Pdef(\x,Pbind(\dur,Pseq([0.5,0.5,0.5,0.5,1.25,0.75],inf)));
		});
		if(n==6,{
			Pdef(\x).quant_(4);
			Pdef(\x,Pbind(\dur,Pseq([0.5,0.5,0.5,0.5,1.25,0.5,0.25],inf)));
		});
		if(n==7,{
			Pdef(\x).quant_(4);
			Pdef(\x,Pbind(\dur,Pseq([0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,1.25,0.25,0.25,0.25],inf)));
		});
		if(n==8,{
			Pdef(\x).quant_(4);
			Pdef(\x,Pbind(\dur,Pseq([1,2,1],inf)));
		});
		if(n==9,{
			Pdef(\x).quant_(4);
			Pdef(\x,Pbind(\dur,Pseq([3,1],inf)));
		});
		if(n==10,{
			Pdef(\x).quant_(4);
			Pdef(\x,Pbind(\dur,Pseq([4],inf)));
		});
		if(n==11,{
			Pdef(\x).quant_(4);
			Pdef(\x,Pbind(\dur,Pseq([Rest(4)],inf)));
		});
	}

	*thirdMovement {
		// groove, in C, with a fixed rhythmic pattern (tabla and code only)
		TempoClock.tempo = 112/60;
		// j: isolated notes, soloist and orchestra, descending, free time
		// try to use different instruments for each note, or at least make a different sound
		// C B A  G E F# D C
		// 96 84 72 60 48 36... (24)
	}

	*k {
		Tdef(\thirdMovement, {
			// k is 196 beats (7 times 7 bars times 4 beats)
			Pdef(\passacaglia,Pbind(
				\instrument,\chebyBass,
				\midinote,Pseq([24],1),
				\dur,28,\legato,1.05,
				\out,0
			)).play(quant:4);
			28.wait;
			Pdef(\passacaglia,Pbind(
				\instrument,\chebyBass,
				\midinote,Pseq([24,26,30,28,31,26,23],inf),
				\dur,4,\legato,1.05,
				\out,0
			)).play(quant:4);
			Sinfonia.thirdMaterial(0); 28.wait;
			Sinfonia.thirdMaterial(1); 28.wait;
			Sinfonia.thirdMaterial(2); 28.wait;
			Sinfonia.thirdMaterial(3); 28.wait;
			Sinfonia.thirdMaterial(4); 28.wait;
			Sinfonia.thirdMaterial(5); 28.wait;

			// l: voice enters
			Sinfonia.thirdMaterial(6); 196.wait;

			// m: delay of voice decays, beats are most intense
			Sinfonia.thirdMaterial(7); 196.wait;

			// n:
			Sinfonia.thirdMaterial(8); 84.wait;
			Sinfonia.thirdMaterial(9); 56.wait;
			Sinfonia.thirdMaterial(10); 56.wait;
			Sinfonia.thirdMaterial(11);
		}).play(quant:1);
	}
}
