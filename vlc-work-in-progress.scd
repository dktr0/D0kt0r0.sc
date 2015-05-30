Vlc.meow(\nime2015);
Vlc.test
Pbindef(\test,\db,-2,\dur,0.125,\midinote,(40..84));
Pbindef(\test).stop
~tabla = -30
Vlc.stopRecording
Vlc.quit

Vlc.meow;
(instrument:\point,out:Vlc.to(0),db:-1).play; // works
(instrument:\point,out:Vlc.to(4),db:-1).play; // works
~test.to(0)
~test.stop
~saw = { Saw.ar(110,-20.dbamp) }
~saw.to(0);
~saw.to(4);
~saw.to([0,4]);
~saw.stop(10)
~tabla = -20; // works: inputs 0 & 1 go to 12 & 13
Vlc.stopRecording
Vlc.quit

// TODO:
// still need to try new SC build and supernova on second computer!!!
// metering tailored to the situation (with ge gi na tun, etc)
// better Synthdefs
// lots of practice & tests, including with recording and screencap going

TempoClock.tempo = 104/60
~pitch = 26
~pitch.fadeTime = 0.8
~lead = { LFTri.ar((~pitch.ar+[0,4]+~lfo.ar).midicps,mul:-300.dbamp); }
~lead.playN([0,4]+Vlc.offset);
~lead2 = { LFTri.ar(DelayN.ar(~pitch.ar+7,4,4).midicps,mul:-302.dbamp); }
~lead2.playN([0,4]+Vlc.offset);
~saw = { Saw.ar(~pitch.ar.midicps,mul:-1.dbamp)*~ge.ar; }
~saw.playN([1,5]+Vlc.offset);
~saw2 = { Saw.ar((~pitch.ar+12).midicps,mul:-1.dbamp)*~gi.ar; }
~saw2.playN([2,6]+Vlc.offset);
~saw3 = { Saw.ar((~pitch.ar+24).midicps,mul:-1.dbamp)*~na.ar; }
~saw3.playN([3,7]+Vlc.offset);
~saw4 = { Saw.ar((~pitch.ar+27).midicps,mul:-1.dbamp)*~tun.ar; }
~saw4.playN([3,7]+Vlc.offset);






