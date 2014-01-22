	  ___  _       _          ______                 
	 / _ \| |     | |         |  ___|                
	/ /_\ \ |_ __ | |__   __ _| |_ ___  _ __ ___ ___ 
	|  _  | | '_ \| '_ \ / _` |  _/ _ \| '__/ __/ _ \
	| | | | | |_) | | | | (_| | || (_) | | | (_|  __/
	\_| |_/_| .__/|_| |_|\__,_\_| \___/|_|  \___\___|
	        | |                                      
	        |_|                                      

# AlphaForceFramework
## Corey Matyas
Licensed under the MIT License - See COPYING for full license text

AlphaForceFramework is a full Java IRC framework I created for my entry in the [dreamincode.net](http://www.dreamincode.net/) IRC bot-making contest in 2011.
I had the full source published on Github originally, but for some reason I seem to have deleted the repository. Hosting this back up here to aid other 
bot makers in the development of IRC bots.

### Features
* Interface to the IRC protocol
* Network I/O concurrency
* Supports CTCP
* Event system with support for custom handlers for IRC events

### Development
This code is not currently under active development, but I will review pull requests.
The original git repository has since been lost, so treat the initial commit as more of a version 1.0.
It is not completely abandoned, however. I hope to eventually fix a few of the things that seemed like good design decisions 3 years ago.

### Planned Revisions
* Clean up concurrency
* Rewrite IRCBot.processChat()
* Remove bot- and network- specific features that worked their way into the framework
* Revise logging
* Add support for regex command filters

### Special Thanks
I wouldn't have had any motivation to write this bot nor the people guiding me along if it weren't for the active members of dreamincode.net's IRC channel.
Shoutouts to all of ##dreamincode (now on freenode), but specific thanks to:

* XAMPP
* creativecoding
* no2pencil
* macosxnerd
* Motoma