// Based on the DE10-Lite

package spinalttl

import spinal.core._

class Toplevel extends Component {
  val io = new Bundle {
    val KEY0 = in Bool
    val KEY1 = in Bool
    val LEDR = out UInt(10 bits)
  }

  // We need specific signal names here
  noIoPrefix()

  // Setup the clock for the counter
  val coreClockDomain = new ClockDomain(
    clock = io.KEY0,
    config = ClockDomainConfig(
      clockEdge        = FALLING,
    )
  )

  // Create our counter
  val counterArea = new ClockingArea(coreClockDomain) {
      private val counter = new Counter(10)

      // Tie the counter to the toplevel names
      io.LEDR <> counter.io.value.resize(10 bits)
      counter.io.reset := !io.KEY1
  }
}

object Toplevel {
  def main(args: Array[String]) {
    SpinalVerilog(new Toplevel)
  }
}
