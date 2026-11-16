#!/usr/bin/env bash
# Carga mascotas de demostración equivalentes a las del mockData del frontend.
# Los dueños usan los ids de los usuarios mock del front: 2 = Ignacio, 3 = Camila, 4 = Felipe.
# Uso:  ./scripts/seed-demo.sh [URL_BASE]      (por defecto http://localhost:8083)
set -euo pipefail

BASE="${1:-http://localhost:8083}"
API="$BASE/api/pets"

# especie: 1 = DOG, 2 = CAT, 3 = BIRD, 4 = Other
# formato: nombre|especieId|raza|edad(años)|peso(kg)|ownerId
PETS=(
  "Rocky|1|Labrador|4|28.5|3"
  "Luna|1|Quiltro|7|14.0|2"
  "Nala|1|Quiltro|3|12.3|3"
  "Mango|2|Quiltro|9|4.8|4"
  "Pocha|1|Maltés|10|5.1|2"
  "Monona|1|Cocker Spaniel|4|11.7|3"
  "Roxy|1|Quiltro|5|13.2|4"
)

for row in "${PETS[@]}"; do
  IFS='|' read -r name species breed age weight owner <<< "$row"
  birth="$(date -d "-${age} years" +%F)"
  code=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$API" \
    -H 'Content-Type: application/json' \
    -d "{\"name\":\"$name\",\"speciesId\":$species,\"breed\":\"$breed\",\"birthDate\":\"$birth\",\"weight\":$weight,\"ownerId\":$owner}")
  echo "$name -> HTTP $code"
done
