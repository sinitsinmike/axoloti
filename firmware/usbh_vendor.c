/// basdd on the quirk added to te ALSA linux kernel
// https://lkml.org/lkml/2010/2/10/25


#include "usbh_midi_core.h"

#define _USB_H_
#include "ch.h"

USB_Setup_TypeDef MIDI_Setup;

#define MIDI_MIN_READ_POLL 1
#define MIDI_MIN_WRITE_POLL 1

#define USB_VENDOR_CLASS_ID 0xFF


// implementation ids
#define USB_ACCESS_VID 0x133e
#define USB_VIRUS_PID 0x815
#define USB_ZOOM_VID 0x1686
#define USB_R24_IO_PID 0xDD
#define USB_R24_SLAVE_PID 0xDE


static USBH_StatusTypeDef USBH_Virus_InterfaceInit(USBH_HandleTypeDef *phost);
static USBH_StatusTypeDef USBH_R24_InterfaceInit(USBH_HandleTypeDef *phost);
static USBH_StatusTypeDef USBH_Vendor_InterfaceInit(USBH_HandleTypeDef *phost);

// Note: bwlow will need to chnage if non-midi implementations are used
USBH_ClassTypeDef  Vendor_Class = {
  "VEN",
  USB_VENDOR_CLASS_ID,
  USBH_Vendor_InterfaceInit,
  USBH_MIDI_InterfaceDeInit,
  USBH_MIDI_ClassRequest,
  USBH_MIDI_Process,
  USBH_MIDI_SOFProcess,
  NULL,
};

static USBH_StatusTypeDef USBH_Vendor_InterfaceInit(USBH_HandleTypeDef *phost) {

    USBH_StatusTypeDef status = USBH_NOT_SUPPORTED;

    if(phost->device.DevDesc.idVendor == USB_ACCESS_VID && phost->device.DevDesc.idProduct == USB_VIRUS_PID) {
       	USBH_UsrLog("USB Access Virus detected");
        return USBH_Virus_InterfaceInit(phost);
    }
    if(phost->device.DevDesc.idVendor == USB_ZOOM_VID) {
      if(phost->device.DevDesc.idProduct == USB_R24_IO_PID) {
       	USBH_UsrLog("USB Zoom R24 Audio I/F detected");
        return USBH_R24_InterfaceInit(phost);
      }
      else if(phost->device.DevDesc.idProduct == USB_R24_SLAVE_PID) {
       	USBH_UsrLog("USB Zoom R24 Slave detected");
        return status;
      }
    }
    return status;
}



static USBH_StatusTypeDef USBH_Virus_InterfaceInit(USBH_HandleTypeDef *phost) {
    MIDI_HandleTypeDef *MIDI_Handle;
    USBH_StatusTypeDef status = USBH_FAIL;

    const uint8_t interface = 5;

    usbh_midi_init(); 

    if(interface<phost->device.CfgDesc.bNumInterfaces) {
        if( (phost->device.CfgDesc.Itf_Desc[interface].bInterfaceClass == USB_VENDOR_CLASS_ID) &&
            (phost->device.CfgDesc.Itf_Desc[interface].bInterfaceSubClass == 0) ) {

            // bizarre, why select the interface... all it does is put it in current interface and then log it,
            // but we may not even actually use it !?
            USBH_SelectInterface(phost, interface);
            phost->pActiveClass->pData = (MIDI_HandleTypeDef *)USBH_malloc(sizeof(MIDI_HandleTypeDef));
            MIDI_Handle = phost->pActiveClass->pData;
            MIDI_Handle->state_in = MIDI_INIT;
            MIDI_Handle->state_out = MIDI_INIT;


            uint8_t num_ep = ((phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].bNumEndpoints <= USBH_MAX_NUM_ENDPOINTS)
                              ? phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].bNumEndpoints
                              : USBH_MAX_NUM_ENDPOINTS);

            MIDI_Handle->input_valid = false;
            MIDI_Handle->output_valid = false;

            uint8_t i=0;
            
            for (; i< num_ep && (!isValidInput(MIDI_Handle) || !isValidOutput(MIDI_Handle)) ; i++) {
                
                bool bInput = phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].Ep_Desc[i].bEndpointAddress & 0x80;
                if(!isValidInput(MIDI_Handle) && bInput) {
                    MIDI_Handle->InEp = phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].Ep_Desc[i].bEndpointAddress;
                    MIDI_Handle->InEpSize  = phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].Ep_Desc[i].wMaxPacketSize;
                	USBH_UsrLog("USB Host (Virus) Input size requests : %x", MIDI_Handle->InEpSize );
                    if(MIDI_Handle->InEpSize >USBH_MIDI_EPS_IN_SIZE) MIDI_Handle->InEpSize = USBH_MIDI_EPS_IN_SIZE;
//                    MIDI_Handle->read_poll = phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].Ep_Desc[i].bInterval;
//                	USBH_UsrLog("USB Host (Virus) Input interval : %i", MIDI_Handle->read_poll);
//                    if(MIDI_Handle->read_poll<MIDI_MIN_READ_POLL) MIDI_Handle->read_poll = MIDI_MIN_READ_POLL;
                    MIDI_Handle->input_valid = true;
                }
                if(!isValidOutput(MIDI_Handle) && !bInput) {
                    MIDI_Handle->OutEp = phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].Ep_Desc[i].bEndpointAddress;
                    MIDI_Handle->OutEpSize  = phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].Ep_Desc[i].wMaxPacketSize;
                	USBH_UsrLog("USB Host (Virus) Output size requests : %x", MIDI_Handle->OutEpSize );
                    if(MIDI_Handle->OutEpSize >USBH_MIDI_EPS_OUT_SIZE) MIDI_Handle->OutEpSize = USBH_MIDI_EPS_OUT_SIZE;
//                    MIDI_Handle->write_poll = phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].Ep_Desc[i].bInterval;
//                	USBH_UsrLog("USB Host (Virus) Output interval : %i", MIDI_Handle->write_poll);
//                    if(MIDI_Handle->write_poll<MIDI_MIN_WRITE_POLL) MIDI_Handle->write_poll = MIDI_MIN_WRITE_POLL;
                    MIDI_Handle->output_valid = true;
                }
                
            } // each endpoint, or until ive found both input and output endpoint



            if (isValidOutput(MIDI_Handle)) {
                USBH_UsrLog("USB Host (Virus) Output connected to %x : %x", interface, MIDI_Handle->OutEp );

                MIDI_Handle->OutPipe = USBH_AllocPipe(phost, MIDI_Handle->OutEp);
                USBH_OpenPipe  (phost,
                                MIDI_Handle->OutPipe,
                                MIDI_Handle->OutEp,
                                phost->device.address,
                                phost->device.speed,
                                USB_EP_TYPE_BULK,
                                MIDI_Handle->OutEpSize);
                USBH_LL_SetToggle  (phost, MIDI_Handle->OutPipe,0);

                USBH_UsrLog("Switch Virus to USB mode");
                static uint8_t seq[] = { 0x4e, 0x73, 0x52, 0x01 }; 
                USBH_BulkSendData(phost, seq, sizeof(seq), MIDI_Handle->OutPipe,false);

                // prime output ring buffer for use
                usbh_midi_reset_buffer();
            }
            
            if (isValidInput(MIDI_Handle)) {
                USBH_UsrLog("USB Host (Virus) Input connected to %x : %x", interface, MIDI_Handle->InEp );

                MIDI_Handle->InPipe = USBH_AllocPipe(phost, MIDI_Handle->InEp);
                USBH_OpenPipe  (phost,
                                MIDI_Handle->InPipe,
                                MIDI_Handle->InEp,
                                phost->device.address,
                                phost->device.speed,
                                USB_EP_TYPE_BULK,
                                MIDI_Handle->InEpSize);
                USBH_LL_SetToggle  (phost, MIDI_Handle->InPipe,0);
            }
            status = USBH_OK;

            // ring buffer ready to use
            //send_ring_buffer.read_ptr  = send_ring_buffer.write_ptr = 0;

            return status;
        } // if, a midi interface
  
    }// for interface 3
    
  return status;
}



static USBH_StatusTypeDef USBH_R24_InterfaceInit(USBH_HandleTypeDef *phost) {
    MIDI_HandleTypeDef *MIDI_Handle;
    USBH_StatusTypeDef status = USBH_FAIL;

    uint8_t interface = 0;

    usbh_midi_init(); 
    for(interface=0; interface<phost->device.CfgDesc.bNumInterfaces && interface < USBH_MAX_NUM_INTERFACES; interface++) {
        if( (phost->device.CfgDesc.Itf_Desc[interface].bInterfaceClass == USB_VENDOR_CLASS_ID) &&
            (phost->device.CfgDesc.Itf_Desc[interface].bInterfaceSubClass == 3) ) {

            // bizarre, why select the interface... all it does is put it in current interface and then log it,
            // but we may not even actually use it !?
            USBH_SelectInterface(phost, interface);
            phost->pActiveClass->pData = (MIDI_HandleTypeDef *)USBH_malloc(sizeof(MIDI_HandleTypeDef));
            MIDI_Handle = phost->pActiveClass->pData;
            MIDI_Handle->state_in = MIDI_INIT;
            MIDI_Handle->state_out = MIDI_INIT;


            uint8_t num_ep = ((phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].bNumEndpoints <= USBH_MAX_NUM_ENDPOINTS)
                              ? phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].bNumEndpoints
                              : USBH_MAX_NUM_ENDPOINTS);

            // eventually we should be looking for multiple input and output EP, for output we then just write to the one indicated
            // by the CID, for READ we may have to consider allowing the user to select which ports they are interested in (for efficiency?)
            // but for the moment just pick the first input and the first output
            MIDI_Handle->input_valid = false;
            MIDI_Handle->output_valid = false;

            uint8_t i=0;
            for (; i< num_ep && (!isValidInput(MIDI_Handle) || !isValidOutput(MIDI_Handle)) ; i++) {
                bool bInput = phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].Ep_Desc[i].bEndpointAddress & 0x80;
                if(!isValidInput(MIDI_Handle) && bInput) {
                    MIDI_Handle->InEp = phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].Ep_Desc[i].bEndpointAddress;
                    MIDI_Handle->InEpSize  = phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].Ep_Desc[i].wMaxPacketSize;
                    USBH_UsrLog("USB Host Input size requests : %x", MIDI_Handle->InEpSize );
                    MIDI_Handle->InEpSize = USBH_MIDI_EPS_IN_SIZE; // why bother reducing the size? Some devices will lie about the max ep size...
//                    MIDI_Handle->read_poll = phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].Ep_Desc[i].bInterval;
//                  USBH_UsrLog("USB Host Input interval : %i", MIDI_Handle->read_poll);
//                    if(MIDI_Handle->read_poll<MIDI_MIN_READ_POLL) MIDI_Handle->read_poll = MIDI_MIN_READ_POLL;
                    MIDI_Handle->input_valid = true;
                }
                if(!isValidOutput(MIDI_Handle) && !bInput) {
                    MIDI_Handle->OutEp = phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].Ep_Desc[i].bEndpointAddress;
                    MIDI_Handle->OutEpSize  = phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].Ep_Desc[i].wMaxPacketSize;
                    USBH_UsrLog("USB Host Output size requests : %x", MIDI_Handle->OutEpSize );
                    if(MIDI_Handle->OutEpSize >USBH_MIDI_EPS_OUT_SIZE) MIDI_Handle->OutEpSize = USBH_MIDI_EPS_OUT_SIZE;
//                    MIDI_Handle->write_poll = phost->device.CfgDesc.Itf_Desc[phost->device.current_interface].Ep_Desc[i].bInterval;
//                  USBH_UsrLog("USB Host Output interval : %i", MIDI_Handle->write_poll);
//                    if(MIDI_Handle->write_poll<MIDI_MIN_WRITE_POLL) MIDI_Handle->write_poll = MIDI_MIN_WRITE_POLL;
                    MIDI_Handle->output_valid = true;
                }

            } // each endpoint, or until ive found both input and output endpoint



            if (isValidOutput(MIDI_Handle)) {
                USBH_UsrLog("USB Host Output connected to %x : %x", interface, MIDI_Handle->OutEp );

                MIDI_Handle->OutPipe = USBH_AllocPipe(phost, MIDI_Handle->OutEp);
                USBH_OpenPipe  (phost,
                                MIDI_Handle->OutPipe,
                                MIDI_Handle->OutEp,
                                phost->device.address,
                                phost->device.speed,
                                USB_EP_TYPE_BULK,
                                MIDI_Handle->OutEpSize);
                USBH_LL_SetToggle  (phost, MIDI_Handle->OutPipe,0);

                // ring buffer ready to use
                usbh_midi_reset_buffer();
            }
 
            if (isValidInput(MIDI_Handle)) {
                USBH_UsrLog("USB Host Input connected to %x : %x", interface, MIDI_Handle->InEp );

                MIDI_Handle->InPipe = USBH_AllocPipe(phost, MIDI_Handle->InEp);
                USBH_OpenPipe  (phost,
                                MIDI_Handle->InPipe,
                                MIDI_Handle->InEp,
                                phost->device.address,
                                phost->device.speed,
                                USB_EP_TYPE_BULK,
                                MIDI_Handle->InEpSize);
                USBH_LL_SetToggle  (phost, MIDI_Handle->InPipe,0);
            }
            status = USBH_OK;


            return status;
        } // if, a midi interface

    }// for each interface
    
  return status;
}
