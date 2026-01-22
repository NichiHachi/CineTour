#!/usr/bin/env python3
"""
Script pour nettoyer et corriger tous les fichiers CSV d'export
- Corrige les données mal formatées dans movies.csv et principals.csv
- Supprime les relations orphelines dans principals.csv (films inexistants)
"""

import re
import sys
from pathlib import Path


def fix_movies_csv(filepath):
    """Corrige le fichier movies.csv en supprimant les patterns '); (num' dans num_votes"""
    print(f"🔧 Correction de {filepath}...")

    with open(filepath, "r", encoding="utf-8") as f:
        lines = f.readlines()

    header = lines[0]
    data_lines = lines[1:]

    fixed_count = 0
    fixed_lines = [header]

    for line in data_lines:
        # Pattern: remplacer "num); (num" par "num" à la fin de la ligne
        original = line
        line = re.sub(r"(\d+)\); \(\d+$", r"\1", line.rstrip()) + "\n"

        if line != original:
            fixed_count += 1

        fixed_lines.append(line)

    with open(filepath, "w", encoding="utf-8") as f:
        f.writelines(fixed_lines)

    print(f"✅ {fixed_count} lignes corrigées dans movies.csv")
    return fixed_count


def fix_principals_csv_format(filepath):
    """Corrige le fichier principals.csv en fusionnant les lignes cassées et supprimant les lignes NULL"""
    print(f"🔧 Correction du format de {filepath}...")

    with open(filepath, "r", encoding="utf-8") as f:
        lines = f.readlines()

    header = lines[0]
    data_lines = lines[1:]

    # Compter le nombre de pipes dans l'en-tête
    expected_pipes = header.count("|")

    removed_count = 0
    merged_count = 0
    fixed_lines = [header]
    current_line = ""

    for line in data_lines:
        line = line.rstrip("\n")

        # Supprimer les lignes qui commencent par NULL|
        if line.startswith("NULL|"):
            removed_count += 1
            continue

        # Si on a une ligne en attente, on l'ajoute à celle-ci
        if current_line:
            current_line = current_line + " " + line
        else:
            current_line = line

        # Compter le nombre de pipes dans la ligne courante
        pipe_count = current_line.count("|")

        # Si on a le bon nombre de pipes, on écrit la ligne
        if pipe_count == expected_pipes:
            if not current_line.startswith(" "):
                fixed_lines.append(current_line + "\n")
                current_line = ""
            else:
                merged_count += 1
        elif pipe_count > expected_pipes:
            # Trop de pipes, on saute
            removed_count += 1
            current_line = ""

    # Ligne incomplète à la fin
    if current_line:
        removed_count += 1

    with open(filepath, "w", encoding="utf-8") as f:
        f.writelines(fixed_lines)

    print(f"✅ {removed_count} lignes supprimées et {merged_count} lignes fusionnées")
    return removed_count + merged_count


def load_movie_ids(movies_csv_path):
    """Charge tous les IDs de films depuis movies.csv"""
    print(f"\n📋 Chargement des IDs de films depuis {movies_csv_path}...")
    movie_ids = set()

    with open(movies_csv_path, "r", encoding="utf-8") as f:
        header = f.readline().strip()
        columns = header.split("|")

        # Trouver l'index de id_imdb:ID(Movie)
        id_imdb_idx = None
        for idx, col in enumerate(columns):
            if "id_imdb" in col or ":ID(Movie)" in col:
                id_imdb_idx = idx
                break

        if id_imdb_idx is None:
            print("❌ ERREUR: Colonne id_imdb non trouvée")
            sys.exit(1)

        # Lire tous les IDs de films
        for line in f:
            parts = line.strip().split("|")
            if len(parts) > id_imdb_idx:
                movie_id = parts[id_imdb_idx]
                if movie_id:
                    movie_ids.add(movie_id)

    print(f"✅ {len(movie_ids):,} films chargés")
    return movie_ids


def clean_principals_orphans(principals_csv_path, output_csv_path, movie_ids):
    """Nettoie les relations orphelines dans principals.csv"""
    print(f"\n🧹 Nettoyage des relations orphelines dans {principals_csv_path}...")

    total_lines = 0
    kept_lines = 0
    removed_lines = 0

    with (
        open(principals_csv_path, "r", encoding="utf-8") as infile,
        open(output_csv_path, "w", encoding="utf-8") as outfile,
    ):
        # Copier l'en-tête
        header = infile.readline()
        outfile.write(header)
        columns = header.strip().split("|")

        # Trouver l'index de :END_ID(Movie)
        end_id_idx = None
        for idx, col in enumerate(columns):
            if ":END_ID(Movie)" in col:
                end_id_idx = idx
                break

        if end_id_idx is None:
            print("❌ ERREUR: Colonne :END_ID(Movie) non trouvée")
            sys.exit(1)

        # Traiter chaque ligne
        for line in infile:
            total_lines += 1
            parts = line.strip().split("|")

            if len(parts) > end_id_idx:
                movie_id = parts[end_id_idx]

                # Vérifier si le film existe
                if movie_id in movie_ids:
                    outfile.write(line)
                    kept_lines += 1
                else:
                    removed_lines += 1
            else:
                # Ligne malformée
                outfile.write(line)
                kept_lines += 1

            # Afficher la progression
            if total_lines % 500000 == 0:
                print(
                    f"  Traité: {total_lines:,} lignes | Gardées: {kept_lines:,} | Supprimées: {removed_lines:,}"
                )

    print("\n✅ Nettoyage terminé:")
    print(f"   Total: {total_lines:,} lignes")
    print(f"   Gardées: {kept_lines:,} ({kept_lines / total_lines * 100:.1f}%)")
    print(
        f"   Supprimées: {removed_lines:,} ({removed_lines / total_lines * 100:.1f}%)"
    )

    return kept_lines, removed_lines


def main():
    csv_dir = Path(__file__).parent.parent / "csv-export"

    print("=" * 70)
    print("🧹 NETTOYAGE COMPLET DES FICHIERS CSV")
    print("=" * 70)
    print(f"Répertoire: {csv_dir}\n")

    # 1. Correction de movies.csv
    movies_csv = csv_dir / "movies.csv"
    if movies_csv.exists():
        fix_movies_csv(movies_csv)
    else:
        print(f"⚠️  {movies_csv} n'existe pas, ignoré")

    print()

    # 2. Correction du format de principals.csv
    principals_csv = csv_dir / "principals.csv"
    if principals_csv.exists():
        fix_principals_csv_format(principals_csv)
    else:
        print(f"❌ {principals_csv} n'existe pas")
        sys.exit(1)

    # 3. Chargement des IDs de films
    if not movies_csv.exists():
        print(f"❌ {movies_csv} n'existe pas")
        sys.exit(1)

    movie_ids = load_movie_ids(movies_csv)

    # 4. Nettoyage des relations orphelines
    principals_clean = csv_dir / "principals.csv.clean"
    kept, removed = clean_principals_orphans(
        principals_csv, principals_clean, movie_ids
    )

    # 5. Remplacement du fichier original
    principals_backup = csv_dir / "principals.csv.bak.old"
    principals_csv.rename(principals_backup)
    principals_clean.rename(principals_csv)

    print("\n" + "=" * 70)
    print("✅ NETTOYAGE TERMINÉ AVEC SUCCÈS")
    print("=" * 70)
    print(f"📄 Backup créé: {principals_backup.name}")
    print(f"📊 Relations valides: {kept:,} / {kept + removed:,}")


if __name__ == "__main__":
    main()
