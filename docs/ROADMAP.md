# FastASCII Roadmap

### Phase 1: Core Foundation (Current)
- Implement scalar Pure Java parsing routines.
- Establish the 5 core components (`Writer`, `Reader`, `Scanner`, `UTF8`, `Char`).
- Eliminate `StringBuilder` allocations in `FastTerminalRenderer`.

### Phase 2: Ecosystem Adoption
- Port `FastANSI` and `AnsiMouse` to use FastASCII.
- Port `FastJSON` for zero-copy JSON parsing.
- Port `FastGLOB` for SIMD path pattern matching.
