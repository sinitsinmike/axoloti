# Compiling from source{#compile}

# Windows {#compile_on_win}

<b>
Important
    Regular users are recommended to download an install a release version, rather than compiling from source. Releases are published at <a href="https://github.com/axoloti/axoloti/releases">https://github.com/axoloti/axoloti/releases</a>.
</b> 

## Install MSYS2 {#compile_on_win_msys2}
Download and install msys2-i686 from <a href="http://msys2.github.io/">http://msys2.github.io/</a>.
Use the default settings. Follow the instructions on the webpage.
		
## Install JDK {#compile_on_win_jdk}
Download and run the JDK installer from <a href="http://www.oracle.com/technetwork/java/javase/downloads/index.html">http://www.oracle.com/technetwork/java/javase/downloads/index.html</a>
pick "jdk-8u...-windows-i586.exe".
For developers, I recommend downloading and installing the Netbeans with JDK package from <a href="http://www.oracle.com/technetwork/java/javase/downloads/jdk-netbeans-jsp-142931.html">http://www.oracle.com/technetwork/java/javase/downloads/jdk-netbeans-jsp-142931.html</a>

## Get Axoloti from Github repository{#compile_on_win_axoloti_src}
Clone the axoloti repository using the github windows client, or using the command line (the MSYS2 shell provides git command line tools).
~~~
	C:\Users\username> git clone https://github.com/axoloti/axoloti.git
	C:\Users\username> cd axoloti
	C:\Users\username\axoloti> git submodule update --init --recursive      
~~~

## Compiling {#compile_on_win_compiling}
Open a terminal and enter:
~~~
C:\Users\username\axoloti> cd platform_win
C:\Users\username\axoloti> build.bat
C:\Users\username\axoloti> cd ..
~~~

## Launching {#compile_on_win_launchin}
Open, if not opened, a terminal, go to the axoloti directory and run
~~~
C:\Users\username\axoloti> Axoloti.bat
~~~

# OSX {#compile_on_osx}

<b>
Important
    Regular users are recommended to download an install a release version, rather than compiling from source. Releases are published at <a href="https://github.com/axoloti/axoloti/releases">https://github.com/axoloti/axoloti/releases</a>.
</b> 

## Install Java JDK {#compile_on_osx_java}
## Install XCode {#compile_on_osx_xcode}
## Get Axoloti from Github repository {#compile_on_osx_source_code}
Install the GitHub application <a href = "https://mac.github.com/">https://mac.github.com/</a>.
Or if you have git installed, open a terminal, and enter
~~~
~$> git clone https://github.com/axoloti/axoloti.git
~$> cd axoloti
~$> git submodule update --init --recursive
~~~

## Compiling {#compile_on_osx_compile_gui}
Open a terminal, go to the axoloti directory and run
~~~
~$> ant
~~~

If you don't have ant installed, this can be obtained through "brew"
~~~
~$> brew update
~$> brew install ant
~~~
that would require one to have <a href = "http://brew.sh/">http://brew.sh/</a> installed of course...

Open a terminal, go to the axoloti directory and run
~~~
~$> cd platform_osx
~$> ./build.sh
~$> cd ..
~~~

Root privileges are required to add a udev rule to grant access to Axoloti Core on USB. The install script will ask you for this.

## Launching {#compile_on_osx_launching}
Open, if not opened, a terminal, go to the axoloti directory and run
~~~
~$> ./Axoloti.sh
~~~

# Linux {#compile_on_linux}

<b>
Important
    Regular users are recommended to download an install a release version, rather than compiling from source. Releases are published at <a href="https://github.com/axoloti/axoloti/releases">https://github.com/axoloti/axoloti/releases</a>.
</b> 

Assuming Ubuntu Linux. Other Distributions may need minor changes.

## Get Axoloti from Github repository {#compile_on_linux_source}
Open, if not opened, a terminal and enter:
~~~
$> git clone https://github.com/axoloti/axoloti.git
$> cd axoloti
$> git submodule update --init --recursive
~~~

## Compiling {#compile_on_linux_compiling}
Open a terminal and enter:
~~~
$> cd platform_linux
$> ./build.sh
$> cd ..
~~~

Root privileges are required to add a udev rule to grant access to Axoloti Core on USB. The install script will ask you for this.

## Launching {#compile_on_linux_launching}
Open a terminal, go to the axoloti directory and run
~~~
$> ./Axoloti.sh
~~~

