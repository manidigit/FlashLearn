#!/usr/bin/env python3
"""FlashLearn Theme/Design System regression audit.

This is intentionally narrow: it blocks known architectural regressions without
forbidding legitimate Compose logical alignment or data/chart coordinates.
"""
from pathlib import Path
import json, re, sys

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/com/flashlearn/app/ui"
TARGETS = [
    UI/"addword/AddWordMethodScreen.kt",
    UI/"addword/AddWordScreen.kt",
    UI/"addword/BulkImportScreen.kt",
    UI/"backup/BackupScreen.kt",
    UI/"library/LibraryScreenV2.kt",
    UI/"progress/ProgressScreen.kt",
    UI/"review/ReviewScreen.kt",
]
errors=[]

def text(p): return p.read_text(encoding="utf-8")

all_ui = "\n".join(text(p) for p in UI.rglob("*.kt"))
for bad in ['Text("←"', "scaleX", "Icons.Outlined.ArrowBack", "paddingLeft", "paddingRight", "absoluteLeft", "absoluteRight"]:
    if bad in all_ui:
        errors.append(f"forbidden directional workaround remains: {bad}")

for p in UI.rglob("*.kt"):
    s=text(p)
    if "CompositionLocalProvider(" in s and "LocalLayoutDirection" in s and p.name != "MainActivity.kt":
        errors.append(f"screen-local layout direction override: {p.relative_to(ROOT)}")

for p in TARGETS:
    s=text(p)
    if "FlashLearnScreenHeader" not in s:
        errors.append(f"target screen does not use shared ScreenHeader: {p.relative_to(ROOT)}")

# Active app root must be the single direction provider.
main=text(ROOT/"app/src/main/java/com/flashlearn/app/MainActivity.kt")
if main.count("LocalLayoutDirection provides") != 1:
    errors.append("MainActivity must contain exactly one root LocalLayoutDirection provider")

# Samples must remain valid JSON and retain the real parser/export contracts.
sample_specs = {
    "sample_theme.json": ("formatVersion", 2),
    "sample-vocabulary-backup.json": ("schemaVersion", 2),
    "sample-progress-backup.json": ("schemaVersion", 2),
    "sample-full-backup.json": ("schemaVersion", 2),
}
sample_data={}
for name,(key,value) in sample_specs.items():
    p=ROOT/"docs/samples"/name
    try:
        data=json.loads(p.read_text(encoding="utf-8"))
        sample_data[name]=data
        if data.get(key)!=value:
            errors.append(f"{name}: {key} must be {value}")
    except Exception as e:
        errors.append(f"{name}: invalid JSON: {e}")

theme_keys = {"formatVersion","id","name","lightPrimary","darkPrimary","lightSecondary","darkSecondary",
"lightBackground","darkBackground","lightSurface","darkSurface","lightSurfaceVariant","darkSurfaceVariant",
"lightOnSurface","darkOnSurface","lightOnSurfaceVariant","darkOnSurfaceVariant","lightCard","darkCard",
"lightOutline","darkOutline","gradientStart","gradientEnd","iconStyle","elevationScale","cornerSmall",
"cornerMedium","cornerLarge","typographyScale","densityScale"}
if "sample_theme.json" in sample_data and set(sample_data["sample_theme.json"]) != theme_keys:
    errors.append("sample_theme.json: keys do not match FlashLearnThemeSpec.toJson()")

def require_sections(name, sections):
    d=sample_data.get(name)
    if d:
        for section in sections:
            if section not in d or not isinstance(d[section], list):
                errors.append(f"{name}: missing array section {section}")

require_sections("sample-vocabulary-backup.json",
    ["concepts","contents","tags","categories","relations","variants","languages","languagePairs"])
require_sections("sample-progress-backup.json",
    ["learningStates","difficultyStates","reviewSessions","reviewHistory"])
require_sections("sample-full-backup.json",
    ["concepts","contents","learningStates","difficultyStates","tags","conceptTags",
     "reviewSessions","reviewHistory","settings","categories","achievements","parserMetadata",
     "relations","variants","reviewQueue","languages","languagePairs","conceptReferences"])
for name in ["sample-vocabulary-backup.json","sample-full-backup.json"]:
    d=sample_data.get(name,{})
    if d.get("backupType") not in {"VOCABULARY","FULL"}:
        errors.append(f"{name}: invalid backupType")
if sample_data.get("sample-progress-backup.json",{}).get("backupType") != "PROGRESS":
    errors.append("sample-progress-backup.json: invalid backupType")

if errors:
    print("\n".join("ERROR: "+e for e in errors))
    sys.exit(1)
print("Theme/Design System static audit: PASS")
