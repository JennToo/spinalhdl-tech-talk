package spinalttl

import spinal.core._

// A "Component" is a black-box logical element of the overall system.
class AndGate extends Component {
  // Each component accepts some number of input signals and generates some
  // number of output signals. These signals are (by convention) tied to an
  // "io" variable
  val io = new Bundle {
    // Here we declare "a" and "b" to be the input signals, of type Bool (i.e.
    // only a single bit each)
    val a = in Bool
    val b = in Bool

    // And here "output" is declared as a single bit output signal
    val output = out Bool
  }

  // Here we declare the logic of our Component. In this case it's pretty
  // simple: take the values of "a" and "b" and if they're both true, set
  // "output" to true
  io.output := io.a & io.b
}

// This function allows us to run this object to generate the VHDL equivalent
// for our component. When we run this, we'll get an "AndGate.vhd" file that we
// could then import into our FPGA vendor's tools for synthesis etc.
//
// We could also generate Verilog as well, it's just a matter of preference
object AndGate {
  def main(args: Array[String]) {
    SpinalVhdl(new AndGate)
  }
}
