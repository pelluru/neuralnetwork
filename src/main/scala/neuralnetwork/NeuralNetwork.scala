package neuralnetwork

object NeuralNetwork {
    //List of weights under given neuron
    type NeuronWeight = Double
    type NeuronWeights = List[NeuronWeight]
    type WeightMatrix = List[NeuronWeights]
    type Weights = List[Layer]

    abstract class Layer (val layer: List[NeuronWeights])
    case class BiasLayer (override val layer: List[NeuronWeights]) extends Layer (layer)
    case class NoBiasLayer (override val layer: List[NeuronWeights]) extends Layer(layer)

    trait WeightsPrinter {
        def weightsToString (weights: Weights): String =
            (for (layer <- weights) yield
                layerToString(layer.layer) + "\n").mkString

        def layerToString(layer: WeightMatrix): String =
            (for (neuronWeights <- layer) yield
                neuronWeightsToString(neuronWeights) + "| ").mkString

        def neuronWeightsToString(neuronWeights: NeuronWeights): String =
            (for (weight <- neuronWeights) yield
                weight.toString + " ").mkString
    }

    class Neuron(val activationFunction: Double => Double) {
        def calculate(x: Double): Double = activationFunction(x)
    }

    class NeuralNetwork (val weights: Weights, activationFunction: Double => Double) extends WeightsPrinter {
        def checkWeights(weights: Weights): Boolean =
            weights match {
                case Nil => {
                    //this shouldn't happen because it means empty argument list
                    false
                }
                case layer :: Nil => {
                    //nothing to do here, last layer determines only input length
                    true
                }
                case layer :: lowerLayers => {
                    val lowerLayerNeuronsCount = lowerLayers.head.layer.length
                    val isAllRight = (
                        for (neuronWeights <- layer.layer) yield neuronWeights.length == (lowerLayerNeuronsCount + 1)
                    ).forall((bool) => bool)
                    isAllRight && checkWeights(lowerLayers)
                }
            }
        assert(checkWeights(weights))
        val bias = -1.0
        val neuron = new Neuron(activationFunction)
        def scalarProduct(l1: List[Double], l2: List[Double]) = (for {(x, y) <- l1 zip l2} yield x * y).sum
        def calculate(input: List[Double]) = {
            calculate0(input, weights)
        }
        def calculate0(input: List[Double], weights: Weights): List[Double] = {
            weights match {
                case inputLayer :: Nil =>
                    for (neuronWeights <- inputLayer.layer) yield
                        neuron.calculate(scalarProduct(neuronWeights, bias :: input))
                case currentLayer :: lowerLayers => {
                    val precomputed = calculate0(input, lowerLayers)
                    for (neuronWeights <- currentLayer.layer) yield
                        neuron.calculate(scalarProduct(neuronWeights, bias :: precomputed))
                }
                case Nil => throw new IllegalArgumentException("Weight list cannot be empty")
            }
        }
        override def toString =
            weightsToString(weights)
    }
}
