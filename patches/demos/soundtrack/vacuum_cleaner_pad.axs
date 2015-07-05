<patch-1.0>
   <obj type="math/c 32" sha="5797bce9fc4e770d9c14890b0fa899f126c5bc38" name="c321" x="126" y="14">
      <params/>
      <attribs/>
   </obj>
   <obj type="math/c 32" sha="5797bce9fc4e770d9c14890b0fa899f126c5bc38" name="c321_" x="294" y="14">
      <params/>
      <attribs/>
   </obj>
   <obj type="lfo/sine" sha="6215955d70f249301aa4141e75bdbc58d2782ae6" name="osc1" x="14" y="56">
      <params>
         <frac32.s.map name="pitch" onParent="true" value="-8.0"/>
      </params>
      <attribs/>
   </obj>
   <obj type="mix/mix 1" sha="75de53c9e6783829b405b702a6e7feb5ccaa8b00" name="mix11" x="126" y="56">
      <params>
         <frac32.u.map name="gain1" value="14.0"/>
      </params>
      <attribs/>
   </obj>
   <obj type="lfo/sine" sha="6215955d70f249301aa4141e75bdbc58d2782ae6" name="osc1_" x="238" y="56">
      <params>
         <frac32.s.map name="pitch" onParent="true" value="56.5"/>
      </params>
      <attribs/>
   </obj>
   <obj type="mix/mix 1" sha="75de53c9e6783829b405b702a6e7feb5ccaa8b00" name="mix11_" x="336" y="56">
      <params>
         <frac32.u.map name="gain1" value="13.5"/>
      </params>
      <attribs/>
   </obj>
   <obj type="midi/in/bend" sha="84fc4df2ea385612e1294f33f4bfffbc8b501534" name="bendin1" x="28" y="140">
      <params/>
      <attribs/>
   </obj>
   <obj type="midi/in/keyb zone" sha="44dada96531ef6abd5c77f60bb92dbb2ec0d6d35" name="keyb_1" x="14" y="196">
      <params/>
      <attribs>
         <spinner attributeName="startNote" value="0"/>
         <spinner attributeName="endNote" value="127"/>
      </attribs>
   </obj>
   <obj type="math/smooth" sha="3a277a80f7590217e14fde92e834ace04d2b75cb" name="smooth1" x="126" y="196">
      <params>
         <frac32.u.map name="time" onParent="true" value="3.0"/>
      </params>
      <attribs/>
   </obj>
   <obj type="env/ahd" sha="ce83118fedc4aa5d92661fa45a38dcece91fbee4" name="envd1" x="224" y="196">
      <params>
         <frac32.u.map name="a" onParent="true" value="0.0"/>
         <frac32.u.map name="d" onParent="true" value="11.0"/>
      </params>
      <attribs/>
   </obj>
   <obj type="mix/mix 2" sha="90ac1a48634cb998bf3d0387eb5191531d6241fe" name="mix12" x="336" y="196">
      <params>
         <frac32.u.map name="gain1" value="14.5"/>
         <frac32.u.map name="gain2" value="12.0"/>
      </params>
      <attribs/>
   </obj>
   <obj type="osc/pwm" sha="a5f49fd39de0194bff6482e7b17ed3f35174578c" name="pwm_1" x="434" y="196">
      <params>
         <frac32.s.map name="pitch" onParent="true" value="-12.185123443603516"/>
      </params>
      <attribs/>
   </obj>
   <obj type="mix/mix 2" sha="90ac1a48634cb998bf3d0387eb5191531d6241fe" name="mix21" x="658" y="252">
      <params>
         <frac32.u.map name="gain1" value="12.0"/>
         <frac32.u.map name="gain2" value="32.0"/>
      </params>
      <attribs/>
   </obj>
   <obj type="math/smooth" sha="3a277a80f7590217e14fde92e834ace04d2b75cb" name="smooth1_" x="756" y="252">
      <params>
         <frac32.u.map name="time" onParent="true" value="8.0"/>
      </params>
      <attribs/>
   </obj>
   <obj type="env/ahd" sha="ce83118fedc4aa5d92661fa45a38dcece91fbee4" name="envahd1" x="546" y="280">
      <params>
         <frac32.u.map name="a" onParent="true" value="0.0"/>
         <frac32.u.map name="d" onParent="true" value="45.0"/>
      </params>
      <attribs/>
   </obj>
   <obj type="osc/pwm" sha="a5f49fd39de0194bff6482e7b17ed3f35174578c" name="pwm_1_" x="434" y="294">
      <params>
         <frac32.s.map name="pitch" onParent="true" value="-0.01004934310913086"/>
      </params>
      <attribs/>
   </obj>
   <obj type="osc/pwm" sha="a5f49fd39de0194bff6482e7b17ed3f35174578c" name="pwm_1__" x="434" y="406">
      <params>
         <frac32.s.map name="pitch" onParent="true" value="-23.93801259994507"/>
      </params>
      <attribs/>
   </obj>
   <obj type="mix/mix 4" sha="217ea56f47dd7397f64930ffcddab7c794ad4f5c" name="mix31" x="546" y="406">
      <params>
         <frac32.u.map name="gain1" onParent="true" value="12.0"/>
         <frac32.u.map name="gain2" onParent="true" value="12.0"/>
         <frac32.u.map name="gain3" onParent="true" value="12.0"/>
         <frac32.u.map name="gain4" onParent="true" value="0.0"/>
      </params>
      <attribs/>
   </obj>
   <obj type="filter/lp m" sha="649887a8ccb34e5928d77426b8db79bed3e57f0f" name="lpf_1" x="644" y="420">
      <params>
         <frac32.s.map name="pitch" MidiCC="1" value="20.0"/>
         <frac32.u.map name="reso" onParent="true" value="19.0"/>
      </params>
      <attribs/>
   </obj>
   <obj type="gain/vca" sha="6bbeaeb94e74091879965461ad0cb043f2e7f6cf" name="vca_1" x="756" y="420">
      <params/>
      <attribs/>
   </obj>
   <obj type="patch/outlet a" sha="72226293648dde4dd4739bc1b8bc46a6bf660595" name="outlet_1" x="826" y="420">
      <params/>
      <attribs/>
   </obj>
   <nets>
      <net>
         <source name="keyb_1 note"/>
         <dest name="smooth1 in"/>
         <dest name="mix21 in1"/>
      </net>
      <net>
         <source name="mix12 out"/>
         <dest name="pwm_1 pitchm"/>
         <dest name="pwm_1_ pitchm"/>
         <dest name="pwm_1__ pitchm"/>
      </net>
      <net>
         <source name="pwm_1 wave"/>
         <dest name="mix31 in1"/>
      </net>
      <net>
         <source name="pwm_1_ wave"/>
         <dest name="mix31 in2"/>
      </net>
      <net>
         <source name="pwm_1__ wave"/>
         <dest name="mix31 in3"/>
      </net>
      <net>
         <source name="osc1 wave"/>
         <dest name="mix11 in1"/>
      </net>
      <net>
         <source name="c321 o"/>
         <dest name="mix11 bus_in"/>
      </net>
      <net>
         <source name="mix11 out"/>
         <dest name="pwm_1_ pwm"/>
         <dest name="pwm_1__ pwm"/>
      </net>
      <net>
         <source name="smooth1 out"/>
         <dest name="mix12 bus_in"/>
      </net>
      <net>
         <source name="keyb_1 gate"/>
         <dest name="envahd1 gate"/>
         <dest name="envd1 gate"/>
      </net>
      <net>
         <source name="envahd1 env"/>
         <dest name="vca_1 v"/>
      </net>
      <net>
         <source name="c321_ o"/>
         <dest name="mix11_ bus_in"/>
      </net>
      <net>
         <source name="osc1_ wave"/>
         <dest name="mix11_ in1"/>
      </net>
      <net>
         <source name="mix11_ out"/>
         <dest name="pwm_1 pwm"/>
      </net>
      <net>
         <source name="envd1 env"/>
         <dest name="mix12 in1"/>
      </net>
      <net>
         <source name="mix31 out"/>
         <dest name="lpf_1 in"/>
      </net>
      <net>
         <source name="lpf_1 out"/>
         <dest name="vca_1 a"/>
      </net>
      <net>
         <source name="bendin1 bend"/>
         <dest name="mix12 in2"/>
      </net>
      <net>
         <source name="mix21 out"/>
         <dest name="smooth1_ in"/>
      </net>
      <net>
         <source name="smooth1_ out"/>
         <dest name="lpf_1 pitchm"/>
      </net>
      <net>
         <source name="keyb_1 velocity"/>
         <dest name="mix21 in2"/>
      </net>
      <net>
         <source name="vca_1 o"/>
         <dest name="outlet_1 outlet"/>
      </net>
   </nets>
   <settings>
      <subpatchmode>polyphonic</subpatchmode>
      <MidiChannel>1</MidiChannel>
      <HasMidiChannelSelector>true</HasMidiChannelSelector>
      <NPresets>8</NPresets>
      <NPresetEntries>32</NPresetEntries>
      <NModulationSources>8</NModulationSources>
      <NModulationTargetsPerSource>8</NModulationTargetsPerSource>
   </settings>
   <notes><![CDATA[]]></notes>
</patch-1.0>