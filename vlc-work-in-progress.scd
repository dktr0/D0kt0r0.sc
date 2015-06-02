
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


r = 26;
z = -20.dbamp
~a = { LFTri.ar(([26,38,50,62]-26+r).midicps,mul:-20.dbamp) *~ge.ar}
~saw={Saw.ar([26,38,50,62].midicps)*z*~ge.ar}
~b = { LFTri.ar(([38,50,62,74]-26+r).midicps,mul:-20.dbamp) *~gi.ar}
~c = ...
~d = ...








