// Sinfonia, by David Ogborn
// laptop orchestra instructions

// 0. at the beginning of the performance:
s.boot; // boot your scsynth server
// load your collection of SynthDefs
// *** evaluate the region below ***
(
~chord = [ 60, 62, 64, 65, 67 ];
OSCdef(\chord,
			{ |msg,time,addr,port|
				("/sinfonia/chord " ++ msg[1].asString).postln;
				topEnvironment.put(\chord,msg[1].asString.compile.value);
		},"/sinfonia/chord").permanent_(true);
)

// 1. first movement = tacit

// 2. second movement:
// the environment variable ~chord will contain a harmony for you to orchestrate
// your task, at the appointed time in the movement, is to improvise a sparse
// texture of short events (like "pizzicato" notes from strings but perhaps with
// more internal articulation, grain, etc) that always use the notes of ~chord
// (including all octave transpositions, voicings, inversions, etc)

// here are some examples using the default SynthDef:
Pbind(\instrument,\default,\dur,1/12,\midinote,~chord+Prand([-36,0,36],18)).play
Pbind(\instrument,\default,\dur,1/24,\legato,4,\midinote,Prand(~chord,inf),\db,Pseq((-20,-22..-80))).play
// but I suggest using your own instruments, making more complicated patterns etc
// don't hesitate to introduce noisy elements so long as the basic pitch classes are still generally audible

// 3. third movement:
// slow descent through this pitch structure
// pppp most of the time
// use your own synths but generally aim for
// long, subtle, shimmery sounds
// [ 96, 95, 93, 93, 88, 90, 86 ]
// [ 84, 83, 81, 81, 76, 78, 74 ]
// [ 72, 71, 69, 69, 64, 66, 62 ]
// [ 60, 59, 57, 57, 52, 54, 50 ]
// [ 48, 47, 45, 45, 40, 42, 38 ]

// here is an example synth - modify it or make your own with a related character
(
SynthDef(\swell,
	{
		arg freq = 440, out = 0, amp = 0.1, lpf = 2500, sustain = 6.001, fnoise = 0.003;
		var audio = LFTri.ar(freq*Rand(1-fnoise,1+fnoise), mul: amp);
		audio = LPF.ar(audio, freq: lpf);
		audio = audio*EnvGen.ar(Env.linen((sustain-0.001)*0.33,0.001,(sustain-0.001)*0.67), doneAction: 2);
		Out.ar(out,audio);
}).add;
)

// here is an example of invoking the synth above
// you might choose to use low db values and then add the synth repeatedly to increase loudness
// this also helps with getting chorus-y random detuning textures as in this example:
(instrument:\swell,sustain:10,midinote:60+0.03.rand2,db:-50).play

// 4. fourth movement:
// wait for the vocal soloist to enter
// then free improv with the complete set of pitches from the third movement
// try to build in intensity with the vocal soloist
// stop when the vocal soloist finishes the first sung section of this movement
// when the vocal soloist resumes singing (quiet pitches)
