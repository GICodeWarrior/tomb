This is the Tomb Password Manager.

## Features

* Strong encryption
* Cross platform (Requires JRE 8)
* Simple GUI
* Simple CLI
* No browser integration
* No cloud support

## Tomb Format

1. 6 bytes - magic (.TOMB\377)
2. 1 byte - format version (2)
3. 16 bytes - Bcrypt salt
4. 1 byte - Bcrypt cost
5. 12 bytes - AES-GCM IV
6. n bytes - AES-GCM encrypted, deflated, JSON

## License

Tomb is available under MIT License.  See LICENSE.

Individual libraries are available under their own licenses.  Bouncy Castle is available under MIT License.  Apache Commons IO is available under [Apache License 2.0](http://www.apache.org/licenses/).  MiG Layout is available under [3-clause BSD License](http://www.miglayout.com/mavensite/license.html).  JSON-java is available under a modified MIT License.