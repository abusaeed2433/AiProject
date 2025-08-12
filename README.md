## Hex game
A strategic two-player board game where the player competes against an AI agent. Each player is assigned a color (one starting from the left, the other from the right). The objective is to form an unbroken path of their colored tiles connecting their side to the opposite side before the opponent does.
The game includes multiple difficulty levels, allowing players to choose the AI's strength.

## Algorithm Used
- Alpha-Beta Pruning – Optimizes the AI’s decision-making by pruning unnecessary branches in the game tree, enhancing search efficiency.
- Genetic Algorithm – Explores optimal strategies by simulating evolution-inspired selection and mutation processes.
- Fuzzy Logic – Handles uncertainty and makes decisions based on approximate reasoning, used to automatically switch between algorithm.
- Graph Algorithms – Used for pathfinding and evaluating connectivity across the board.



### Project Snapshots

<table>
  <tr>
    <td>
      <img src="https://raw.githubusercontent.com/abusaeed2433/AiProject/main/snapshots/level.jpg" alt="Level selection" width="400">
      <div align="center"><i>Level selection</i></div>
    </td>
    <td>
      <img src="https://raw.githubusercontent.com/abusaeed2433/AiProject/main/snapshots/board.jpg" alt="Board" width="400">
      <div align="center"><i>Board</i></div>
    </td>
  </tr>

  <tr>
    <td>
      <img src="https://raw.githubusercontent.com/abusaeed2433/AiProject/main/snapshots/player.jpg" alt="Gameplay1" width="400">
      <div align="center"><i>Gameplay1</i></div>
    </td>
    <td>
      <img src="https://raw.githubusercontent.com/abusaeed2433/AiProject/main/snapshots/ai.jpg" alt="Gameplay2" width="400">
      <div align="center"><i>Gameplay2</i></div>
    </td>
  </tr>

  <tr>
    <td>
      <img src="https://raw.githubusercontent.com/abusaeed2433/AiProject/main/snapshots/block.jpg" alt="Gameplay3" width="400">
      <div align="center"><i>Gameplay3</i></div>
    </td>
    <td>
      <img src="https://raw.githubusercontent.com/abusaeed2433/AiProject/main/snapshots/bad.jpg" alt="Gameplay4" width="400">
      <div align="center"><i>Gameplay4</i></div>
    </td>
  </tr>

  <tr>
    <td>
      <img src="https://raw.githubusercontent.com/abusaeed2433/AiProject/main/snapshots/end.jpg" alt="Gameplay5" width="400">
      <div align="center"><i>Gameplay5</i></div>
    </td>
    <td>
      <img src="https://raw.githubusercontent.com/abusaeed2433/AiProject/main/snapshots/Music_Player.jpg" alt="Victory" width="400">
      <div align="center"><i>Victory</i></div>
    </td>
  </tr>




</table>






### Directory Structure

```
📂aiproject  
 │───📂classes
 |   │──AlphaBetaApplier.java
 |   │──Calculator.java
 |   │──Cell.java
 |   │──CellState.java
 |   │──FuzzyApplier.java
 |   │──GameBoard.java
 |   │──GeneticApplier.java
 |   │──Helper.java
 |   │──Hexagon.java
 |   │──MyConfuseBar.java
 |   │──MyPair.java
 |   │──MyTextView.java
 |   │──PathScore.java
 |   └──SoundController.java
 |───📂enums
 |   └──PredictionAlgo.java
 |───📂listener
 |   │──AlphaBetaListener.java
 |   └──GeneticListener.java
 │───HomeScreen.java 
 │───MainActivity.java 
 │───PreCalc.java 
 └───SplashScreen.java
```