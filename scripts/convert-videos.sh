#!/bin/bash
# =============================================================================
# convert-videos.sh
#
# Converte arquivos .mp4 para o formato HLS fMP4 usado pelo streaming-service.
#
# Fluxo:
#   1. Lê arquivos .mp4 de:  streaming-service/local/movies-to-convert/
#   2. Cria pasta de saída:   streaming-service/local/bucket/<nome-do-video>/
#   3. Converte para HLS fMP4 (init.mp4 + seg0.m4s … + playlist.m3u8)
#   4. Apaga o .mp4 original de movies-to-convert/
#
# Uso:
#   ./scripts/convert-videos.sh
#
# Após a conversão, suba os vídeos para o S3 com:
#   ./scripts/setup-s3.sh
# =============================================================================

set -euo pipefail

# ── Cores ────────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# ── Caminhos ─────────────────────────────────────────────────────────────────
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
INPUT_DIR="$PROJECT_ROOT/services/streaming-service/local/movies-to-convert"
BUCKET_DIR="$PROJECT_ROOT/services/streaming-service/local/bucket"

# ── Cabeçalho ────────────────────────────────────────────────────────────────
echo -e "\n${BOLD}${BLUE}=== Video Converter — MP4 → HLS fMP4 ===${NC}\n"
echo -e "  ${CYAN}Input :${NC}  $INPUT_DIR"
echo -e "  ${CYAN}Output:${NC}  $BUCKET_DIR"
echo ""

# ── Verificações de dependências ─────────────────────────────────────────────
if ! command -v ffmpeg &> /dev/null; then
    echo -e "${RED}❌ ffmpeg não encontrado. Instale com:${NC}"
    echo -e "   brew install ffmpeg"
    exit 1
fi

if ! command -v ffprobe &> /dev/null; then
    echo -e "${RED}❌ ffprobe não encontrado (vem junto com o ffmpeg).${NC}"
    exit 1
fi

# ── Garante que as pastas existem ────────────────────────────────────────────
mkdir -p "$INPUT_DIR"
mkdir -p "$BUCKET_DIR"

# ── Busca arquivos .mp4 ───────────────────────────────────────────────────────
shopt -s nullglob
MP4_FILES=("$INPUT_DIR"/*.mp4 "$INPUT_DIR"/*.MP4)
shopt -u nullglob

if [ ${#MP4_FILES[@]} -eq 0 ]; then
    echo -e "${YELLOW}⚠  Nenhum arquivo .mp4 encontrado em:${NC}"
    echo -e "   $INPUT_DIR"
    echo -e "\nColoque seus arquivos .mp4 nessa pasta e rode o script novamente."
    exit 0
fi

echo -e "${CYAN}📂 ${#MP4_FILES[@]} arquivo(s) encontrado(s)${NC}\n"

# ── Contadores ────────────────────────────────────────────────────────────────
SUCCESS=0
SKIPPED=0
FAILED=0

# ── Processa cada arquivo ─────────────────────────────────────────────────────
for INPUT_FILE in "${MP4_FILES[@]}"; do

    FILENAME=$(basename "$INPUT_FILE")

    # Sanitiza o nome: minúsculas, espaços e caracteres especiais → underscore
    RAW_NAME="${FILENAME%.*}"
    VIDEO_NAME=$(echo "$RAW_NAME" \
        | tr '[:upper:]' '[:lower:]' \
        | sed 's/[^a-z0-9]/_/g' \
        | sed 's/__*/_/g'        \
        | sed 's/^_//;s/_$//')

    OUTPUT_DIR="$BUCKET_DIR/$VIDEO_NAME"

    echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "  ${CYAN}Arquivo :${NC} $FILENAME"
    echo -e "  ${CYAN}Pasta   :${NC} bucket/$VIDEO_NAME/"

    # Verifica se já foi convertido
    if [ -d "$OUTPUT_DIR" ]; then
        echo -e "  ${YELLOW}⚠  Pasta '$VIDEO_NAME' já existe — pulando.${NC}"
        echo -e "     (remova a pasta manualmente para re-converter)\n"
        (( SKIPPED++ )) || true
        continue
    fi

    # Lê informações do arquivo de origem
    DURATION=$(ffprobe -v quiet -show_entries format=duration \
        -of default=noprint_wrappers=1:nokey=1 "$INPUT_FILE" 2>/dev/null || echo "?")
    if [ "$DURATION" != "?" ]; then
        DURATION_FMT=$(printf '%d:%02d' $((${DURATION%.*}/60)) $((${DURATION%.*}%60)))
        echo -e "  ${CYAN}Duração :${NC} ~$DURATION_FMT"
    fi

    VIDEO_INFO=$(ffprobe -v quiet -select_streams v:0 \
        -show_entries stream=codec_name,width,height,avg_frame_rate \
        -of default=noprint_wrappers=1 "$INPUT_FILE" 2>/dev/null || echo "")
    AUDIO_INFO=$(ffprobe -v quiet -select_streams a:0 \
        -show_entries stream=codec_name,sample_rate,channels \
        -of default=noprint_wrappers=1 "$INPUT_FILE" 2>/dev/null || echo "")

    echo -e "  ${CYAN}Vídeo   :${NC} $(echo "$VIDEO_INFO" | tr '\n' ' ')"
    echo -e "  ${CYAN}Áudio   :${NC} $(echo "$AUDIO_INFO" | tr '\n' ' ')"
    echo ""

    # Cria pasta de saída
    mkdir -p "$OUTPUT_DIR"

    echo -e "  ${YELLOW}⚙  Convertendo...${NC}"
    START_TIME=$(date +%s)

    # ── Conversão fMP4 HLS ────────────────────────────────────────────────────
    #  -c:v copy / -c:a copy  → stream copy (sem re-encoding, qualidade original)
    #  -hls_time 4            → segmentos de ~4 segundos
    #  -hls_segment_type fmp4 → formato fMP4 (.m4s) igual ao movie1
    #  -hls_fmp4_init_filename → init.mp4 separado
    #  -hls_segment_filename  → seg0.m4s, seg1.m4s, …
    #  -hls_flags independent_segments → cada segmento decodificável sozinho
    # ─────────────────────────────────────────────────────────────────────────
    if ffmpeg -i "$INPUT_FILE" \
        -c:v copy \
        -c:a copy \
        -f hls \
        -hls_time 4 \
        -hls_playlist_type vod \
        -hls_segment_type fmp4 \
        -hls_fmp4_init_filename init.mp4 \
        -hls_segment_filename "$OUTPUT_DIR/seg%d.m4s" \
        -hls_flags independent_segments \
        -hls_list_size 0 \
        "$OUTPUT_DIR/playlist.m3u8" \
        -loglevel error \
        -stats 2>&1 | grep -E "time=|error" || true; then

        END_TIME=$(date +%s)
        ELAPSED=$((END_TIME - START_TIME))
        SEGMENT_COUNT=$(ls "$OUTPUT_DIR"/seg*.m4s 2>/dev/null | wc -l | tr -d ' ')
        TOTAL_SIZE=$(du -sh "$OUTPUT_DIR" 2>/dev/null | cut -f1)

        echo -e "\n  ${GREEN}✓ Conversão concluída em ${ELAPSED}s${NC}"
        echo -e "  ${GREEN}✓ $SEGMENT_COUNT segmentos + init.mp4 + playlist.m3u8${NC}"
        echo -e "  ${GREEN}✓ Tamanho total: $TOTAL_SIZE${NC}"

        # Remove o arquivo original
        rm "$INPUT_FILE"
        echo -e "  ${GREEN}✓ Original removido: $FILENAME${NC}"

        (( SUCCESS++ )) || true

    else
        echo -e "\n  ${RED}❌ Falha na conversão de '$FILENAME'${NC}"
        rm -rf "$OUTPUT_DIR"
        (( FAILED++ )) || true
    fi

    echo ""
done

# ── Resumo final ─────────────────────────────────────────────────────────────
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BOLD}Resumo:${NC}"
echo -e "  ${GREEN}✓ Convertidos : $SUCCESS${NC}"
[ $SKIPPED -gt 0 ] && echo -e "  ${YELLOW}⚠ Ignorados   : $SKIPPED${NC}"
[ $FAILED  -gt 0 ] && echo -e "  ${RED}✗ Falhas      : $FAILED${NC}"
echo ""

if [ $SUCCESS -gt 0 ]; then
    echo -e "${CYAN}Próximo passo — suba os vídeos para o S3:${NC}"
    echo -e "  ${BOLD}./scripts/setup-s3.sh${NC}"
fi

echo ""

