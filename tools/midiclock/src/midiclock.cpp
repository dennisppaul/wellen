#include <iostream>
#include <cstdlib>
#include <chrono>
#include <algorithm>
#include "RtMidi.h"

using namespace std::chrono;
typedef std::chrono::high_resolution_clock Clock;

#if defined(WIN32)
    #include <windows.h>
    // @todo(implement microseconds)
    #define SLEEP( mikroseconds ) Sleep( (DWORD) mikroseconds / 1000 ) 
#else
    /* unix ( incl macOS ) */
    #include <unistd.h>
    #define SLEEP( mikroseconds ) usleep( (unsigned long) ( mikroseconds ) )
#endif

#ifdef __LINUX_ALSA__
#endif
#ifdef __MACOSX_CORE__
#endif

// These functions should be embedded in a try/catch block in case of
// an exception.  It offers the user a choice of MIDI ports to open.
// It returns false if there are no ports available.
// bool chooseInputPort( RtMidiIn *rtmidi );
// bool chooseOutputPort( RtMidiOut *rtmidi );
// 
// void mycallback( double deltatime, std::vector< unsigned char > *message, void *user )
// {
//   unsigned int *clock_count = reinterpret_cast<unsigned int*>(user);
// 
//   // Ignore longer messages
//   if (message->size() != 1)
//     return;
// 
//   unsigned int msg = message->at(0);
//   if (msg == 0xFA)
//     std::cout << "START received" << std::endl;
//   if (msg == 0xFB)
//     std::cout << "CONTINUE received" << std::endl;
//   if (msg == 0xFC)
//     std::cout << "STOP received" << std::endl;
//   if (msg == 0xF8) {
//     if (++*clock_count == 24) {
//       double bpm = 60.0 / 24.0 / deltatime;
//       std::cout << "One beat, estimated BPM = " << bpm <<std::endl;
//       *clock_count = 0;
//     }
//   }
//   else
//     *clock_count = 0;
// }
// 
// int clock_in()
// {
//   RtMidiIn *midiin = 0;
//   unsigned int clock_count = 0;
// 
//   try {
// 
//     // RtMidiIn constructor
//     midiin = new RtMidiIn();
// 
//     // Call function to select port.
//     if ( chooseInputPort( midiin ) == false ) goto cleanup;
// 
//     // Set our callback function.  This should be done immediately after
//     // opening the port to avoid having incoming messages written to the
//     // queue instead of sent to the callback function.
//     midiin->setCallback( &mycallback, &clock_count );
// 
//     // Don't ignore sysex, timing, or active sensing messages.
//     midiin->ignoreTypes( false, false, false );
// 
//     std::cout << "\nReading MIDI input ... press <enter> to quit.\n";
//     char input;
//     std::cin.get(input);
// 
//   } catch ( RtMidiError &error ) {
//     error.printMessage();
//   }
// 
//  cleanup:
// 
//   delete midiin;
// 
//   return 0;
// }

RtMidiOut *midiout = 0;
bool mVerboseMode = false;

int init_rtmidi() {
    try {
        midiout = new RtMidiOut();
    }
    catch ( RtMidiError &error ) {
        error.printMessage();
        exit( EXIT_FAILURE );
    }
    return 0;
}

void MIDI_start() {
    std::vector<unsigned char> message;
//     message.clear();
    message.push_back( 0xFA );
    midiout->sendMessage( &message );
    if (mVerboseMode) std::cout << "MIDI start" << std::endl;
}

void MIDI_stop() {
    std::vector<unsigned char> message;
//     message.clear();
    message.push_back( 0xFC );
    midiout->sendMessage( &message );
    if (mVerboseMode) std::cout << "MIDI stop" << std::endl;
}

void MIDI_continue() {
    std::vector<unsigned char> message;
//     message.clear();
    message.push_back( 0xFB );
    midiout->sendMessage( &message );
    if (mVerboseMode) std::cout << "MIDI continue" << std::endl;
}

void MIDI_clock() {
    std::vector<unsigned char> message;
//     message.clear();
    message.push_back( 0xF8 );
    midiout->sendMessage( &message );
}

int init_clock_out(uint8_t pMIDIPort, uint8_t pBPM) {
 
    midiout->openPort( pMIDIPort );

    const double PPQ = 24.0; // pules per quarter note (PPQ)
    const double USEC_PER_MINUTE = (60 * 1000 * 1000);
    const uint32_t sleep_usec_ideal =  USEC_PER_MINUTE / ( pBPM * PPQ ); // = uSec/pulse
    uint32_t mSleepMicroSec = sleep_usec_ideal;

    if (mVerboseMode) std::cout << "ideal sleep duration in uSec: " << sleep_usec_ideal << std::endl;
    if (mVerboseMode) std::cout << "generating clock at " << (60.0 / PPQ / mSleepMicroSec * 1000.0 * 1000.0) << " BPM." << std::endl;

    MIDI_start();
    
    uint32_t TMP_counter = 0;
    auto start_time = Clock::now();
    auto end_time = start_time;

    uint32_t k = 0; 
    while (true) {
//     for (int k=0; k < PPQ * 8; k++) {
// @todo(maybe add option to terminate after n beats?)
// @todo(maybe add option to receive MIDI commands like BPM or play, pause, continue, stop as SysEx or CC)
        MIDI_clock();
        if (TMP_counter % (int)PPQ == 0) {
            if (mVerboseMode) std::cout << "MIDI clock (one beat)" << std::endl;
            if (!mVerboseMode) std::cout << "." << std::flush;
        }
        TMP_counter++;

        end_time = Clock::now();
        auto mDelta = std::chrono::duration_cast<std::chrono::microseconds>(end_time - start_time).count();
        SLEEP(mSleepMicroSec - mDelta);
        start_time = Clock::now();
    }

    MIDI_continue();
    MIDI_stop();

    delete midiout;

    return 0;
}

template<typename RT>
bool choosePort( RT *rtmidi, const char *dir ) {
    std::string portName;
    unsigned int i = 0, nPorts = rtmidi->getPortCount();
    if ( nPorts == 0 ) {
        std::cerr << "+++ WARNING" << std::endl;
        std::cerr << "+++ no " << dir << " ports available!" << std::endl;
        return false;
    }

    if ( nPorts == 1 ) {
        std::cout << "\nOpening " << rtmidi->getPortName() << std::endl;
    }
    else {
    for ( i=0; i<nPorts; i++ ) {
        portName = rtmidi->getPortName(i);
        std::cout << "  " << dir << " port #" << i << ": " << portName << '\n';
    }

    do {
        std::cout << "\nChoose a port number: ";
        std::cin >> i;
    } while ( i >= nPorts );
    }

    std::cout << "\n";
    rtmidi->openPort(i);

    return true;
}

template<typename RT> bool print_ports( RT *rtmidi, const char *dir ) {
    std::string portName;
    unsigned int i = 0, nPorts = rtmidi->getPortCount();
    if ( nPorts == 0 ) {
        std::cout << "No " << dir << " ports available!" << std::endl;
        return false;
    }

    std::cout << "Available " << dir << " ports:" << '\n';
    for ( i=0; i<nPorts; i++ ) {
        portName = rtmidi->getPortName(i);
        std::cout << "  " << i << " :: " << portName << '\n';
    }

    return true;
}

template<typename RT> int get_port_ID_from_name( RT *rtmidi, const char *dir, std::string pMIDIPortStr) {
    std::string portName;
    unsigned int i = 0, nPorts = rtmidi->getPortCount();
    if ( nPorts == 0 ) {
        return -1;
    }

    for (int i = 0; i<nPorts; i++ ) {
        portName = rtmidi->getPortName(i);
        if((portName.compare(pMIDIPortStr)) == 0) {
            return i;
        }
    }

    return -1;
}

// bool chooseInputPort( RtMidiIn *rtmidi )
// {
//   return choosePort<RtMidiIn>( rtmidi, "input" );
// }

// bool chooseOutputPort( RtMidiOut *rtmidi ) {
//   return choosePort<RtMidiOut>( rtmidi, "output" );
// }

// from https://stackoverflow.com/questions/865668/parsing-command-line-arguments-in-c#868894
class InputParser{
    public:
        InputParser (int &argc, const char **argv){
            for (int i=1; i < argc; ++i)
                this->tokens.push_back(std::string(argv[i]));
        }

        const std::string& getCmdOption(const std::string &option) const{
            std::vector<std::string>::const_iterator itr;
            itr =  std::find(this->tokens.begin(), this->tokens.end(), option);
            if (itr != this->tokens.end() && ++itr != this->tokens.end()){
                return *itr;
            }
            static const std::string empty_string("");
            return empty_string;
        }

        bool cmdOptionExists(const std::string &option) const{
            return std::find(this->tokens.begin(), this->tokens.end(), option)
                   != this->tokens.end();
        }
    private:
        std::vector <std::string> tokens;
};

int main( int argc, const char *argv[] ) {

    init_rtmidi();

    InputParser input(argc, argv);
    if (input.cmdOptionExists("-h")) {
        std::cout << "MIDI clock generator" << std::endl;
        std::cout << std::endl;
        std::cout << "Usage:" << std::endl;
#ifdef __LINUX_ALSA__
        std::cout << "  midiclock.linux [OPTION]" << std::endl;
#endif
#ifdef __MACOSX_CORE__
        std::cout << "  midiclock.macos [OPTION]" << std::endl;
#endif
        std::cout << std::endl;
        std::cout << "Options:" << std::endl;
        std::cout << "  -s                  set speed in beats per minute (BPM)" << std::endl;
        std::cout << "  -p                  MIDI port as string ( use quotes or escape spaces ) or index" << std::endl;
        std::cout << "  -d                  print MIDI ports" << std::endl;
        std::cout << "  -v                  run in verbose mode" << std::endl;
        std::cout << std::endl;
        std::cout << "Example:" << std::endl;
#ifdef __LINUX_ALSA__
        std::cout << "  midiclock.linux -s 140 -p \"Midi Through:Midi Through Port-0 14:0\""<< std::endl;
#endif
#ifdef __MACOSX_CORE__
        std::cout << "  midiclock.macos -s 140 -p \"IAC Driver Bus 1\""<< std::endl;
#endif
        return 0;
    }

    if (input.cmdOptionExists("-d")) {
        print_ports<RtMidiOut>( midiout, "output" );
        return 0;
    }

    if (input.cmdOptionExists("-v")) {
        mVerboseMode = true;
    }

    /* MIDI port */
    int16_t mMIDIPortI = 0;
    const std::string &mMIDIPortStr = input.getCmdOption("-p");
    if (!mMIDIPortStr.empty()) {
        try {
            mMIDIPortI = std::stoi(mMIDIPortStr);
            if (mVerboseMode) std::cout << "-p=" << mMIDIPortI << std::endl;
        } catch (...) {
            // find port ID from name
            // else choose from CLI?
            mMIDIPortI = get_port_ID_from_name(midiout, "output", mMIDIPortStr);
            if (mMIDIPortI < 0) {
                std::cerr << "+++ ERROR" << std::endl;
                std::cerr << "+++ could not find port: \"" << mMIDIPortStr << "\"" << std::endl;
                std::cerr << "+++ hint:" << std::endl;
                std::cerr << "+++ make sure to escape spaces and other special characters." << std::endl;
                std::cerr << "+++ alternatively, surround port name with quotation marks." << std::endl;
                std::cerr << std::endl;
                print_ports<RtMidiOut>( midiout, "output" );
                return 23;
            }
            if (mVerboseMode) std::cout << "-p=" << mMIDIPortStr << " (ID:" << mMIDIPortI << ")" << std::endl;
        }
    } else {
        if (mVerboseMode) std::cout << "-p=" << mMIDIPortI << "(default)" << std::endl;
    }

    /* BPM */
    uint16_t mBPM = 120;
    const std::string &mBPMStr = input.getCmdOption("-s");
    if (!mBPMStr.empty()) {
        mBPM = std::stoi( mBPMStr );
        if (mVerboseMode) std::cout << "-s=" << mBPM << std::endl;
    } else {
        if (mVerboseMode) std::cout << "-s=" << mBPM << "(default)" << std::endl;
    }

    std::cout << "Starting MIDI clock: " << std::flush;
    return init_clock_out(mMIDIPortI, mBPM);
}
