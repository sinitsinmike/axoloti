# Getting started {#getting_started}

# Getting started on OSX {#getting_started_on_osx}
Assumes you have installed from source or downloaded a precompiled binary.
## Start Axoloti
doubleclick axoloti.command in the axoloti directory
## Compile firmware
menu board -> firmware -> compile
## Flash the firmware
Get the board in DFU-mode by holding button S1 while plugging the USB cable.<p>
Select menu board -> firmware -> upload firmware with DFU 
Get the board out of DFU mode (unplug it from your computer and then plug it back in).
## Connect
If the connect checkbox turns on, but the software stops responding, check if there's an sdcard left in the board and remove it... It probably contained an incompatible start.bin file.
## Next
You're ready to use Axoloti. Next time, you can just start Axoloti and click connect, no need to compile or flash firmware again.        

# Getting started on Windows {#getting_started_on_win}
Assumes you have installed from source or downloaded a precompiled binary.
## Install DFU driver
To flash firmware with the DFU method, you need to install the libusbK driver for the "STM32 Bootloader" device (you get this device by holding button S1 while connecting the USB cable).
Use <A href="http://zadig.akeo.ie/">zadig</A> to install the "libusbK" driver for the "STM32 Bootloader" (vendor = 0x0483, product = 0xdf11). Enable options->list all devices to be able to select the "STM32 Bootloader".
## Launching Axoloti
Open (doubleclick) Axoloti.bat
## Compiling the firmware
Select menu "Board", submenu "Firmware", item "Compile firmware".
## Uploading the firmware
Get the board in DFU-mode (hold button S1 while plugging the USB cable).
Select menu "Board", submenu "Firmware", item "Upload firmware with DFU".
Flashing takes a few minutes.
## Connect
If the connect checkbox turns on, but the software stops responding, check if there's an sdcard left in the board and remove it... It probably contained an incompatible start.bin file.
## Next
You're ready to use Axoloti. Next time, you can just start Axoloti and click connect, no need to compile or flash firmware again.        


	
	