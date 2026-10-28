#!/usr/bin/env python3
"""
Prépare la mise à jour de l'APPLICATION (sans Play Store) dans le dossier du site Netlify.

Usage :
    python publier_apk.py Baiboly-JKMM-1.3.0.apk 6 1.3.0 "Nouveautés : ..." [dossier_du_site]

  - le 1er argument : l'APK signé
  - 6      : le versionCode (celui de app/build.gradle.kts, toujours plus grand que le précédent)
  - 1.3.0  : le versionName
  - le texte : ce que les membres verront dans la fenêtre « Version vaovao »
  - dossier_du_site (facultatif) : par défaut « baiboly-contenu »

Le script copie l'APK dans le dossier, met à jour manifest.json (en gardant la partie « Bible »)
et crée une page d'accueil avec un bouton de téléchargement.
Ensuite, glissez tout le dossier sur Netlify.
"""
import hashlib, json, shutil, sys
from pathlib import Path

if len(sys.argv) not in (5, 6):
    sys.exit(__doc__)

apk, code, name, notes = Path(sys.argv[1]), int(sys.argv[2]), sys.argv[3], sys.argv[4]
site = Path(sys.argv[5] if len(sys.argv) == 6 else "baiboly-contenu")
if not apk.exists():
    sys.exit(f"APK introuvable : {apk}")
site.mkdir(exist_ok=True)

dest_name = f"Baiboly-JKMM-{name}.apk"
shutil.copy(apk, site / dest_name)
sha = hashlib.sha256((site / dest_name).read_bytes()).hexdigest()

mf = site / "manifest.json"
manifest = json.loads(mf.read_text(encoding="utf-8")) if mf.exists() else {}
manifest["app"] = {"versionCode": code, "versionName": name, "file": dest_name, "sha256": sha, "notes": notes}
mf.write_text(json.dumps(manifest, indent=2, ensure_ascii=False), encoding="utf-8")

(site / "index.html").write_text(f"""<!DOCTYPE html>
<html lang="mg"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Baiboly JKMM</title></head>
<body style="font-family:Georgia,serif;text-align:center;padding:2.5rem 1.2rem;color:#7A1230;background:#FFF8EC">
<h1>Baiboly JKMM</h1>
<p>Baiboly Masina · Dikanteny 1865</p>
<p><a href="{dest_name}" style="display:inline-block;background:#7A1230;color:#fff;padding:14px 26px;border-radius:30px;text-decoration:none;font-weight:bold">Hisintona ny application (version {name})</a></p>
<p style="font-size:14px;color:#7a5f4f">Aorian'ny fisintonana, sokafy ny rakitra ary avelao ny fametrahana avy amin'ity loharano ity raha angatahin'ny Android.</p>
</body></html>""", encoding="utf-8")
print(f"OK : {dest_name} ({apk.stat().st_size // 1024} Ko), versionCode {code}. Glissez le dossier « {site} » sur Netlify.")
