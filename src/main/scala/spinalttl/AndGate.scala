package spinalttl

import spinal.core._

class AndGate extends Component {
  val io = new Bundle {
    val a = in Bool
    val b = in Bool
    val output = out Bool
  }

  io.output := io.a & io.b
}

// Use this to generate the VHDL
object AndGate {
  def main(args: Array[String]) {
    SpinalVhdl(new AndGate)
  }
}
