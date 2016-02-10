// Sinfonia, by David Ogborn
// laptop orchestra instructions

// Installation:
// Place Sinfonia.sc in your extensions directory
// If you're not sure where that is you can evaluate the next line:
Platform.userExtensionDir;

// 0. at the beginning of the performance:

s = Server.default = Server.local.boot; // boot your scsynth server
// if you have a library of SynthDefs
Sinfonia.start; // this allows your SuperCollider instance to receive network messages

// 1. first movement = tacit

// 2. second movement:
Sinfonia.secondMovement;
// in performance, you don't evaluate the above line
// because it will be evaluated by the live coding soloist
// but you can do it yourself when practicing "at home"
// and in a sectional rehearsal one person should do it for the others
// now ~chord will be a set of midinotes that change across the ensemble
~chord; // try it!

// your task, at the appointed time in the movement, is to improvise a sparse
// texture of short events (like "pizzicato" notes from strings but perhaps with
// more internal articulation, grain, etc) that always use the notes of ~chord
// (including all octave transpositions, voicings, inversions, etc)
// here are some examples:

Pbind(\instrument,\default,\dur,1/12,\midinote,~chord+Prand([-36,0,36],18)).play
Pbind(\instrument,\default,\dur,1/24,\legato,4,\midinote,Prand(~chord,inf),\db,Pseq((-20,-22..-80))).play

// but I suggest using your own instruments, making more complicated patterns etc
// don't hesitate to introduce noisy elements so long as the basic pitch classes are still audible most of the time

// 3. third movement = tacit

// 4. fourth movement:
Sinfonia.fourthMovement; // again, don't do this in performance, but for practice/rehearsal/etc
// during the fourth movement, your task is to write code that responds to rhythmic events
// (your events happen when an OSC message /sinfonia/orchestra arrives, but in this SuperCollider
// implementation you don't need to worry about that... just redefine the Tdef(\orchestra), i.e:

(
Tdef(\orchestra, {
	(instrument:\default,midinote:~chord).play
});
)

// as with the second movement, please use your own instruments (SynthDefs),
// and feel free to change the octave of any or all notes,
// introduce small tuning discrepancies, etc

