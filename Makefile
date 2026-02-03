# ==============================================================================
# Makefile for PIF Project (SAE32_2025)
# Architecture: MVC (models, views, controllers)
# ==============================================================================
# Usage:
#  - make                									: Compiles the entire project
#  - make run-conv ARGS="res/gervais.jpg res/test.pif" 		: Runs the Converter (Encoder)
#  - make run-view ARGS="res/test.pif" 						: Runs the Viewer (Decoder)
#  - make run            									: Runs the Main Menu (Launcher)
#  - make clean        										: Cleans compiled files and JARs
#  - make jar          										: Creates executable JARs
#  - make doc          										: Generates Javadoc
# ==============================================================================

# --- Variables ---

# Tools
JC = javac
JVM = java
JAR = jar

# Directories
SRC_DIR = src
BUILD_DIR = build
RES_DIR = res
DOC_DIR = doc

# Packages
PACKAGE_PATH = fr.iutfbleau.sae32_2025
CONTROLLERS = $(PACKAGE_PATH).controllers

# Entry Points (Classes)
MAIN_APP = $(PACKAGE_PATH).Main
MAIN_CONV = $(CONTROLLERS).Converter
MAIN_DECO = $(CONTROLLERS).Decoder

# Output JAR names
JAR_APP = PIF_App.jar
JAR_CONV = Converter.jar
JAR_DECO = Viewer.jar

# Compilation Flags
JFLAGS = -d $(BUILD_DIR) -sourcepath $(SRC_DIR) -encoding UTF-8

# Sources (Find all .java files)
SOURCES = $(shell find $(SRC_DIR) -name "*.java")

# Marker file to track compilation state (Prevents re-compilation)
BUILD_MARKER = $(BUILD_DIR)/.build_done

# ==============================================================================
# TARGETS
# ==============================================================================

# Default target
default: compile

# ------------------------------------------------------------------------------
# 1. Compilation
# ------------------------------------------------------------------------------
# Only recompiles if sources are newer than the marker file
compile: $(BUILD_MARKER)

$(BUILD_MARKER): $(SOURCES)
	@echo "--- Preparing Resources ---"
	@mkdir -p $(BUILD_DIR)
	@# Copy icons to build folder so AppTheme can find them in classpath
	@cp -r $(RES_DIR)/icons $(BUILD_DIR)/
	@echo "--- Compiling Sources ---"
	$(JC) $(JFLAGS) $(SOURCES)
	@touch $(BUILD_MARKER)
	@echo "--- Compilation Successful. Classes in $(BUILD_DIR) ---"

# ------------------------------------------------------------------------------
# 2. Execution
# ------------------------------------------------------------------------------

# Runs the Main Menu (HomeFrame)
# NOTE: Changed ';' to ':' for WSL/Linux compatibility
run: compile
	@echo "--- Launching Main Application ---"
	$(JVM) -cp "$(BUILD_DIR):$(RES_DIR)" $(MAIN_APP)

# Runs the Converter directly
# Usage: make run-conv ARGS="res/image.jpg"
run-conv: compile
	@echo "--- Launching Converter ---"
	$(JVM) -cp "$(BUILD_DIR):$(RES_DIR)" $(MAIN_CONV) $(ARGS)

# Runs the Viewer directly
# Usage: make run-view ARGS="res/image.pif"
run-view: compile
	@echo "--- Launching Viewer ---"
	$(JVM) -cp "$(BUILD_DIR):$(RES_DIR)" $(MAIN_DECO) $(ARGS)

# ------------------------------------------------------------------------------
# 3. JAR Creation
# ------------------------------------------------------------------------------
jar: compile
	@echo "--- Creating Viewer JAR ($(JAR_DECO)) ---"
	$(JAR) cvfe $(JAR_DECO) $(MAIN_DECO) -C $(BUILD_DIR) .
	
	@echo "--- Creating Converter JAR ($(JAR_CONV)) ---"
	$(JAR) cvfe $(JAR_CONV) $(MAIN_CONV) -C $(BUILD_DIR) .
	
	@echo "--- Creating Main App JAR ($(JAR_APP)) ---"
	$(JAR) cvfe $(JAR_APP) $(MAIN_APP) -C $(BUILD_DIR) .
	
	@echo "--- JARs Created Successfully ---"

# ------------------------------------------------------------------------------
# 4. Cleaning
# ------------------------------------------------------------------------------
clean:
	@echo "--- Cleaning Project ---"
	rm -rf $(BUILD_DIR)
	rm -f *.jar
	rm -rf $(DOC_DIR)

# ------------------------------------------------------------------------------
# 5. Documentation
# ------------------------------------------------------------------------------
doc:
	@echo "--- Generating Javadoc ---"
	mkdir -p $(DOC_DIR)
	javadoc -d $(DOC_DIR) -sourcepath $(SRC_DIR) -subpackages $(PACKAGE_PATH) -encoding UTF-8 -charset UTF-8