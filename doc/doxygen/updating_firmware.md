# Updating firmware {#updating_firmware}

After downloading a new update, you need to recompile the firmware and flash the Axoloti Core.

## Compile the firmware
Go to the Axoloti main window, and in the "board" menu, select firmware->compile.
If all goes well, the message "compile successful!" appears in the log.
## Connect the board in DFU mode
Unplug the Axoloti board from the USB port. Push and hold switch S1 while plugging in the USB connection.
You can release the switch after plugging.
## Flash the firmware
Go to the Axoloti main window, and in the "board" menu, select firmware->flash with DFU. 
If this fails on Windows, use <a href="http://zadig.akeo.ie/">zadig</a> to install the "libusbK" driver for the "STM32 Bootloader" (vendor = 0x0483, product = 0xdf11).
If this fails on Linux, this is likely caused by having insufficient permissions to the USB device. The solution is to run "sudo ./platform_linux/upload_fw_dfu.sh" from the main directory.