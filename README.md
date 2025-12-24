# Bughisweeper
![Java](https://img.shields.io/badge/Java-8-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Android](https://img.shields.io/badge/Android-12L-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min%20SDK-21-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Target SDK](https://img.shields.io/badge/Target%20SDK-32-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-7.0+-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Material Design](https://img.shields.io/badge/Material%20Design-1.5.0-757575?style=for-the-badge&logo=material-design&logoColor=white)

An advanced Android implementation of the classic Minesweeper game, reimagined with educational features, mathematical analysis tools, and unique gameplay enhancements.
> **Note**: This project was designed and implemented by me, with selective use of AI tools for research, refactoring, and productivity - similar to modern IDE assistance.

## About

Bughisweeper transforms the traditional mine-sweeping puzzle into an educational experience that teaches probability theory, Bayesian inference, and information theory through interactive gameplay. The project demonstrates proficiency in Android development, game logic implementation, and UI/UX design.

## Key Features

### Core Gameplay
- **Multiple Difficulty Levels**: Easy, Medium, Hard, and fully customizable board configurations
- **Custom Board Builder**: Create your own challenges with adjustable dimensions (up to 50x50) and mine counts
- **Adaptive UI**: Responsive design with scrollable game boards for various screen sizes
- **Multiple Themes**: Classic, Dark Mode, Forest, Ocean, and Space themes for personalized aesthetics

### Educational Components
- **Real-Time Mathematical Analysis**: Live probability calculations displayed during gameplay
- **Bayesian Inference Engine**: Updates probability estimates based on revealed information
- **Information Theory Integration**: Shannon entropy calculations to identify optimal moves
- **Interactive Math Learning Mode**: Dedicated educational interface with visualization tools
- **Probability Heat Maps**: Visual representation of mine likelihood across the board

### Superpower System
Six unique abilities that add strategic depth to gameplay:
- **Freeze Time**: Pause the timer for strategic planning
- **X-Ray Vision**: Reveal hidden cells temporarily
- **Sonar Pulse**: Detect mines in a radius
- **Lightning Strike**: Automatically reveal the safest cell
- **Shield Mode**: One-time protection from mine detonation
- **Smart Sweep**: AI-assisted move recommendation

### User System
- **Secure Authentication**: SHA-256 password hashing with constraint-based validation
- **Password Strength Analysis**: Real-time entropy-based security assessment
- **Personalized Profiles**: Individual player statistics and preferences
- **Score Tracking**: Comprehensive leaderboard system with difficulty-based rankings

### Statistics & Analytics
- **Game Progress Tracking**: Detailed statistics on wins, losses, and performance metrics
- **Mathematical Insights**: In-game analysis of decision-making patterns
- **Historical Data**: Persistent storage of player achievements and high scores

### Tutorial System
- **Video Demonstrations**: In-app video tutorials for gameplay mechanics
- **Interactive Help**: Comprehensive help system with multiple sections (Basics, Mathematics, Superpowers, Strategy)
- **Mathematical Education**: Dedicated learning module explaining probability concepts

## Technical Highlights

### Architecture & Design
- **MVVM Pattern**: Separation of concerns with clear architectural boundaries
- **Custom View Components**: Optimized `BoardView` with efficient rendering
- **Fragment-Based Navigation**: Modular UI design with ViewPager implementation
- **Material Design**: Adherence to Android Material Design guidelines

### Core Technologies
- **Language**: Java
- **Platform**: Android (API 21+)
- **UI Framework**: AndroidX, Material Components
- **Data Persistence**: SharedPreferences for user data and game state
- **Graphics**: Custom Canvas drawing for game board visualization

### Key Implementations
- **Game Logic Engine**: Recursive cell revelation algorithm with flood-fill optimization
- **Mathematical Calculator**: Real-time probability and entropy computation
- **Animation System**: Smooth transitions and visual feedback
- **Theme Engine**: Dynamic theme switching with resource management
- **Video Player Integration**: Custom video tutorial player with controls

### Security Features
- **Cryptographic Hashing**: SHA-256 implementation for password security
- **Input Validation**: Comprehensive constraint checking for user inputs
- **Secure Session Management**: Protected user authentication state

## Educational Value

This project demonstrates:
- **Probability Theory**: Practical application of statistical concepts
- **Bayesian Reasoning**: Dynamic probability updates based on new information
- **Information Theory**: Shannon entropy for optimal decision-making
- **Algorithm Design**: Efficient flood-fill and pathfinding implementations
- **User Experience Design**: Intuitive interfaces for complex mathematical concepts

## Game Modes

1. **Classic Mode**: Traditional Minesweeper gameplay
2. **Superpower Mode**: Enhanced gameplay with special abilities
3. **Math Mode**: Educational focus with real-time analysis display
4. **Challenge Mode**: Time-based competitive gameplay
5. **Learning Mode**: Tutorial-guided experience for new players

## Development Goals

This project was developed to:
- Demonstrate Android development proficiency
- Implement complex game logic and algorithms
- Create an educational tool for mathematical concepts
- Practice UI/UX design principles
- Showcase software architecture skills

## Requirements

- **Minimum SDK**: Android 5.0 (API 21)
- **Target SDK**: Android 12L (API 32)
- **Compile SDK**: Android 12L (API 32)
- **Java Version**: 8 (source/target compatibility)
- **Permissions**: 
  - `VIBRATE` - Haptic feedback during gameplay
  - `INTERNET` - Reserved for future features (currently unused)
- **Storage**: ~15-20 MB

## Installation & Setup

### Prerequisites
- **Java Development Kit (JDK)**: Version 8 or higher (Java 11 recommended)
- **Android Studio**: Bumblebee or newer
- **Android SDK**: 
  - Minimum API Level 21 (Android 5.0 Lollipop)
  - Target/Compile API Level 32 (Android 12L)
- **Gradle**: Version 7.0+ (bundled with Android Studio)

### Quick Start

```bash
# Clone the repository
git clone https://github.com/bughi104/bughisweeper.git

# Navigate to project directory
cd bughisweeper

# Open in Android Studio
# File → Open → Select the 'bughisweeper' folder

# Sync Gradle files
# Android Studio will prompt you to sync - click "Sync Now"
# Or manually: File → Sync Project with Gradle Files

# Build the project
./gradlew build

# Run on emulator or connected device
# Click the "Run" button (▶️) in Android Studio
# Or use: ./gradlew installDebug
```

### First Launch Notes
- The application will automatically create local SharedPreferences storage on first launch
- Video tutorials are embedded in the app resources (no download needed)
- Create a user account on first launch to access game features
- **Permissions**: The app requires VIBRATE (for haptic feedback) and INTERNET (for potential future features)

### Device Requirements
- **Minimum**: Android 5.0 (API 21) or higher
- **Tested on**: Android 12L (API 32)
- **Orientation**: Portrait mode (game screens)
- **Storage**: ~15-20 MB

### Troubleshooting

**Gradle Sync Issues:**
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

**SDK Path Issues:**
- Create a `local.properties` file in the project root
- Add: `sdk.dir=/path/to/your/Android/Sdk`
- Example (Windows): `sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk`
- Example (Mac/Linux): `sdk.dir=/Users/YourName/Library/Android/sdk`

**Build Errors:**
- Ensure JDK 8+ is set in Android Studio (File → Project Structure → SDK Location)
- Install Android SDK API Level 32 if missing (Tools → SDK Manager)
- Update Android SDK tools if prompted
- Invalidate caches: File → Invalidate Caches / Restart

**Room Database Issues:**
- The app uses Room 2.4.2 for potential future features
- Database files are created automatically in app storage


## How to Play

1. Create an account or log in
2. Select your preferred difficulty or customize your board
3. Tap cells to reveal them - avoid the bugs!
4. Use mathematical analysis to make informed decisions
5. Activate superpowers strategically
6. Complete the board to win and set high scores

## Skills Demonstrated

- Android SDK and AndroidX libraries
- Java programming and OOP principles
- UI/UX design with Material Design
- Custom View development
- Algorithm implementation (flood-fill, pathfinding)
- Mathematical computation integration
- Data persistence and state management
- Security implementation (cryptographic hashing)
- Video playback integration
- Fragment-based architecture
- Resource management and theming
