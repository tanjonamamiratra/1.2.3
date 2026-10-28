#!/usr/bin/env python3
"""
Prépare une mise à jour du contenu de la Bible (sans republier l'APK).

Usage :
    python preparer_mise_a_jour.py baiboly_corrige.db 2

  - baiboly_corrige.db : la base SQLite corrigée (mêmes tables que l'originale)
  - 2                  : le nouveau numéro de version (toujours plus grand que le précédent)

Résultat : un dossier "publication/" contenant
    manifest.json  et  baiboly_mg1865_v2.db
À envoyer dans le dossier "baiboly" de votre hébergement Firebase (voir README.md).
"""
import hashlib, json, shutil, sqlite3, sys
from pathlib import Path

if len(sys.argv) != 3:
    sys.exit(__doc__)

src, version = Path(sys.argv[1]), int(sys.argv[2])
out = Path("publication"); out.mkdir(exist_ok=True)
name = f"baiboly_mg1865_v{version}.db"
dest = out / name
shutil.copy(src, dest)

con = sqlite3.connect(dest)
con.execute("INSERT OR REPLACE INTO meta(cle, valeur) VALUES('version_contenu', ?)", (str(version),))
n = con.execute("SELECT COUNT(*) FROM versets").fetchone()[0]
con.commit(); con.execute("VACUUM"); con.close()
if n < 30000:
    sys.exit(f"Base incomplète : {n} versets seulement. Vérifiez le fichier.")

sha = hashlib.sha256(dest.read_bytes()).hexdigest()
(out / "manifest.json").write_text(
    json.dumps({"version": version, "file": name, "sha256": sha}, indent=2), encoding="utf-8")
print(f"OK : {n} versets, version {version}. Fichiers prêts dans {out}/")
