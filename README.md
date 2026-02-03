# 🖼️ PIF - Primitive Image Format (SAÉ 3.2)

Une suite logicielle complète et élégante pour la compression et la visualisation d'images, développée en **Java (Swing)**. Ce projet utilise l'algorithme de **Huffman Canonique** pour offrir un format de stockage propriétaire optimisé.


## 📖 Sommaire

* [Introduction](#introduction)
* [Fonctionnalités principales](#fonctionnalités-principales)
* [Architecture MVC](#architecture-mvc)
* [Structure du projet](#structure-du-projet)
* [Compilation et exécution](#compilation-et-exécution)
* [Algorithme & Performance](#algorithme--performance)
* [Auteurs](#auteurs)
* [Remerciements](#remerciements)


<a id="introduction"></a>

## 🪶 Introduction

Ce projet a été réalisé dans le cadre de la **SAÉ 3.2 (Semestre 3)** à l'IUT de Fontainebleau. Il a pour objectif la conception et l'implémentation d'un format de fichier d'image propriétaire nommé **PIF (Primitive Image Format)**, ainsi que le développement d'une suite logicielle permettant sa manipulation.

Le format PIF repose sur un algorithme de compression sans perte utilisant le **codage de Huffman Canonique**. Contrairement aux formats standards, il traite les composantes de couleur (Rouge, Vert, Bleu) de manière indépendante pour optimiser le taux de compression tout en garantissant une reconstruction parfaite des données originales.

Pour répondre aux besoins du sujet, l'application se décline en deux outils complémentaires :

* **Le Convertisseur (Encoder) :** permet de transformer des images classiques (PNG, JPG, BMP) en fichiers compressés `.pif` après une analyse statistique des pixels.
* **Le Visualiseur (Decoder) :** permet d'ouvrir, de décoder et de naviguer (zoom et déplacement) dans les images au format `.pif`.

L'ensemble du projet respecte une architecture **MVC** rigoureuse et a été développé avec une attention particulière portée à l'ergonomie et à la portabilité (utilisation d'un Makefile et génération de JARs).

<a id="fonctionnalités-principales"></a>

## 🚀 Fonctionnalités principales

### 🛠️ Converter (Encoder)
* **📊 Analyse Statistique** : Calcul précis de la fréquence d'apparition de chaque intensité de couleur (0-255).
* **🔍 Visualisation Technique** : Affichage dynamique des arbres de Huffman et des codes canoniques générés sous forme de tableaux interactifs.
* **🔨 Ergonomie** : Utilisation de *JMenuBar*, barre d'état, support complet du **Drag & Drop** pour charger une image instantanément.

### 🖼️ Viewer (Decoder)
* **⚙️ Décodage Haute Fidélité** : Reconstruction bit à bit de l'image originale à partir de l'archive PIF.
* **⚓ Navigation Avancée** : Système de rendu interactif permettant le **Zoom centré** (molette) et le **Panoramique** (clic-glisser).
* **😀 Adaptabilité** : Redimensionnement automatique de l'interface en fonction de la résolution de l'image chargée.

<a id="architecture-mvc"></a>

## 🏗️ Architecture MVC

Le projet suit une séparation stricte des responsabilités pour garantir un code propre et maintenable :

* **Modèle (`models/`)** : Le cœur algorithmique. Gère la construction de l'arbre de Huffman, le bit-packing et la manipulation des flux binaires.
* **Vue (`views/`)** : La charte graphique. Utilise les composants Swing avec une attention particulière à l'UX (thème système, polices Roboto/Segoe UI).
* **Contrôleur (`controllers/`)** : Les chef d'orchestres. Lie les interactions utilisateurs aux traitements de données et gère les modes d'exécution (GUI vs CLI).

<a id="structure-du-projet"></a>

## 🗂️ Structure du projet

```bash
PIF_Project/
│
├── src/fr/iutfbleau/sae32_2025/
│   ├── Main.java              # Point d'entrée principal (Launcher)
│   ├── controllers/           # Gestionnaires de flux (Converter, Decoder)
│   ├── models/                # Logique de compression (Huffman, Readers/Writers)
│   └── views/                 # Interfaces Swing (Frames, Panels, Theme)
│
├── res/                       # Ressources visuelles (Icônes, Background)
├── diagrammes/                # Diagrammes UML (PlantUML)
├── build/                     # Classes compilées (généré par Make)
├── doc/                       # Documentation Javadoc (généré)
├── Makefile                   # Automatisation complète du projet
└── *.jar                      # Archives exécutables générées

```

<a id="compilation-et-exécution"></a>

## ⚙️ Compilation et exécution

### 🏗️ Automatisation avec Make

Voici une version mise à jour de la section **Compilation et exécution** de ton `README`. Elle intègre désormais une catégorie dédiée à l'utilisation des fichiers `.jar` sur Windows, ce qui est l'option la plus simple pour un utilisateur final.

---

### ⚙️ Compilation et exécution

L'automatisation du projet est gérée par un `Makefile`. Pour les utilisateurs Windows ne disposant pas de cet outil, nous proposons des commandes alternatives via PowerShell ou l'utilisation directe des archives exécutables.

#### 🐧 Pour Linux / macOS (ou Windows avec Git Bash)

Utilisez l'outil `make` à la racine du projet.

| Commande | Action |
| --- | --- |
| `make` | Compile tout le projet dans le dossier `build/`. |
| `make run` | Lance l'application via le menu d'accueil. |
| `make run-conv ARGS="in.jpg out.pif"` | Compresse une image directement en ligne de commande. |
| `make run-view ARGS="out.pif"` | Décode et affiche un fichier PIF immédiatement. |
| `make jar` | Génère les fichiers JAR exécutables. |
| `make clean` | Supprime les fichiers compilés et les archives. |

#### 🪟 Pour Windows (Utilisation des JARs)

C'est la méthode la plus rapide avec les fichiers **.jar** présents.
Si besoin de regénérer les fichiers **.jar**, voici les commandes :


Pour générer les fichiers **JAR** sur Windows sans passer par le `Makefile`, vous devez utiliser l'outil `jar` fourni avec le JDK.

L'enjeu ici est de créer des archives "exécutables" (en spécifiant un point d'entrée `Main-Class`) et d'inclure les ressources pour qu'elles soient embarquées à l'intérieur du fichier.

Voici les étapes à suivre dans votre terminal (PowerShell ou CMD) :

---

### 1. Préparation (Compilation)

Avant de créer le JAR, il faut que les fichiers `.class` soient générés et que les ressources soient prêtes dans le dossier `build`.

```powershell
# Créer le dossier build et compiler
mkdir build -Force
javac -d build -sourcepath src -encoding UTF-8 src/fr/iutfbleau/sae32_2025/Main.java

# Copier les ressources à l'intérieur du dossier build 
# (essentiel pour que AppTheme les trouve dans le JAR)
xcopy /E /I res\icons build\icons

```

---

### 2. Création des JARs exécutables

Le flag `c` crée l'archive, `v` affiche les détails, `f` spécifie le nom du fichier, et `e` définit le point d'entrée (la classe qui contient le `main`).

---

#### A. Le JAR principal (Launcher)

C'est celui qui lance le menu d'accueil avec toutes les options.

```powershell
jar cvfe PIF_App.jar fr.iutfbleau.sae32_2025.Main -C build .

```

#### B. Le JAR du Convertisseur (Indépendant)

```powershell
jar cvfe Converter.jar fr.iutfbleau.sae32_2025.controllers.Converter -C build .
```

#### C. Le JAR du Visualiseur (Indépendant)

```powershell
jar cvfe Viewer.jar fr.iutfbleau.sae32_2025.controllers.Decoder -C build .
```
---
* **Lancer l'application complète :**
Double-cliquez sur `PIF_App.jar` ou utilisez :
```powershell
java -jar PIF_App.jar
```
* **Lancer le Convertisseur (Mode Batch) :**
```powershell
java -jar Converter.jar image.jpg sortie.pif
# Les arguments "image.jpg" & "sortie.pif" sont optionnels
```
* **Lancer le Visualiseur directement :**
```powershell
java -jar Viewer.jar sortie.pif
# L'argument"sortie.pif" est optionnel
```
#### 🪟 Pour Windows (Compilation et exécution manuelle)

Si vous ne disposez pas de `make`, utilisez ces commandes dans votre terminal (PowerShell ou CMD). 
Cette méthode utilise le **Classpath** pour inclure les ressources (images, icônes) sans avoir à les déplacer.

* **Compiler le projet :**
```powershell
# Créer le dossier build s'il n'existe pas
mkdir build

# Compiler en incluant le dossier src
javac -d build -sourcepath src -encoding UTF-8 src/fr/iutfbleau/sae32_2025/Main.java
```

* **Lancer l'application (Menu Principal) :**
```powershell
java -cp "build;res" fr.iutfbleau.sae32_2025.Main
```

* **Lancer le Convertisseur :**
```powershell
java -cp "build;res" fr.iutfbleau.sae32_2025.controllers.Converter image.jpg sortie.pif
# Les arguments "image.jpg" & "sortie.pif" sont optionnels
```
* **Lancer le Visualisateur :**
```powershell
java -cp "build;res" fr.iutfbleau.sae32_2025.controllers.Decoder sortie.pif
# L'argument"sortie.pif" est optionnel
```

## 📁 JavaDoc

### 🐧 Sur Linux / macOS (ou via Makefile)

Si tu utilises le **Makefile** que nous avons préparé, c'est la méthode la plus simple :

```bash
make doc
```

*Cela va créer un dossier `doc/` à la racine contenant tout le site web de ta documentation.*

---

### 🪟 Sur Windows (PowerShell)

Si vous n'avez pas `make` :

```powershell
# 1. Créer le dossier de destination
mkdir doc 

# 2. Générer la Javadoc
# -d : dossier de destination
# -sourcepath : où se trouve le code
# -subpackages : les packages à inclure
# -encoding : pour bien gérer les accents (UTF-8)
javadoc -d doc -sourcepath src -subpackages fr.iutfbleau.sae32_2025 -encoding UTF-8 -charset UTF-8

```
<a id="algorithme--performance"></a>

## 🧮 Algorithme & Performance

Le projet implémente le **Huffman Canonique**.

- **Avantage** : Au lieu de stocker l'arbre complet dans le fichier (très coûteux), nous ne stockons que les longueurs des codes.
- **Résultat** : Le décodeur reconstruit mathématiquement l'arbre exact, optimisant le gain de compression de manière significative.

<a id="auteurs"></a>

## 👨‍💻 Auteurs

| Nom | Profil | Rôle |
| --- | --- | --- |
| **Canpolat DEMIRCI–ÖZMEN** | [Git](https://grond.iut-fbleau.fr/demircio) | Décodeur & UI/UX Design
| **Maxime ELIOT** | [Git](https://grond.iut-fbleau.fr/eliot) | Logique de Flux, Modèles & Encodeur
| **Luka PLOUVIER** | [Git](https://grond.iut-fbleau.fr/plouvier) | Encodeur & Structure de données + Décodeur

*BUT2 Informatique – 2025/2026 – IUT de Fontainebleau (UPEC)*

<a id="remerciements"></a>

## 🙏 Remerciements

- Merci à Luc HERNANDEZ pour ses cours sur les flux d'octets et les structures de donnnées.
- Merci à Florent MADELAINE pour ses explications sur le codage d'Huffman, son arbre & le fonctionnement du code canonique

---

> ### 🏁 Note finale : ?