#!/bin/bash

# ========== CONFIGURAÇÕES ==========
PROJETO_DIR="/home/integration-middleware"
DESTINO_DIR="/opt/integration-middleware"
JAR_NAME="integration-0.0.1-SNAPSHOT.jar"
PORTA=8080
LOG_FILE="$DESTINO_DIR/logs.txt"
BACKUP_DIR="/opt/backup"

# ========== CORES ==========
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[1;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# ========== FUNÇÕES ==========
parar_aplicacao() {
    echo -e "${YELLOW}⛔ Encerrando processo na porta $PORTA...${NC}"
    sudo fuser -k ${PORTA}/tcp
    echo -e "${GREEN}✔️ Aplicação parada.${NC}"
}

iniciar_aplicacao() {
    echo -e "${BLUE}🚀 Iniciando aplicação com nohup...${NC}"
    cd "$DESTINO_DIR"
    nohup java -jar "target/$JAR_NAME" > "$LOG_FILE" 2>&1 &
    echo -e "${GREEN}✔️ Aplicação iniciada com sucesso!${NC}"
}

reiniciar_aplicacao() {
    parar_aplicacao
    sleep 2
    iniciar_aplicacao
}

compilar_projeto() {
    echo -e "${BLUE}🛠️ Recompilando projeto Maven...${NC}"
    cd "$DESTINO_DIR"
    mvn clean package -DskipTests
    echo -e "${GREEN}✔️ Compilação finalizada.${NC}"
}

copiar_projeto() {
    # Gera backup
    TIMESTAMP=$(date +"%Y-%m-%d_%H-%M")
    BACKUP_FILE="$BACKUP_DIR/integration-middleware-backup-$TIMESTAMP.tar.gz"

    if [ ! -d "$BACKUP_DIR" ]; then
        echo -e "${BLUE}📂 Criando diretório de backup em $BACKUP_DIR...${NC}"
        sudo mkdir -p "$BACKUP_DIR"
    fi

    echo -e "${BLUE}🗂️ Gerando backup da pasta atual em: $BACKUP_FILE${NC}"
    sudo tar -czf "$BACKUP_FILE" -C /opt integration-middleware
    echo -e "${GREEN}✔️ Backup criado com sucesso.${NC}"

    # Copia os arquivos preservando console.sh
    echo -e "${BLUE}📁 Copiando arquivos-fonte do projeto para $DESTINO_DIR...${NC}"
    sudo rsync -av --delete \
        --exclude="target/" \
        --exclude=".git/" \
        --exclude="console.sh" \
        "$PROJETO_DIR/" "$DESTINO_DIR/"
    echo -e "${GREEN}✔️ Arquivos copiados com sucesso.${NC}"

    # Compila e reinicia
    echo -e "${BLUE}🔧 Compilando projeto em $DESTINO_DIR...${NC}"
    cd "$DESTINO_DIR"
    mvn clean package -DskipTests
    echo -e "${GREEN}✔️ Compilação finalizada.${NC}"

    echo -e "${YELLOW}🔄 Reiniciando aplicação...${NC}"
    parar_aplicacao
    sleep 2
    iniciar_aplicacao
}

ver_logs() {
    echo -e "${YELLOW}📜 Últimas 50 linhas do log:${NC}"
    tail -n 50 "$LOG_FILE"
}

log_em_tempo_real() {
    echo -e "${YELLOW}🔄 Exibindo log em tempo real (Ctrl+C para sair)...${NC}"
    tail -f "$LOG_FILE"
}

ver_status() {
    echo -e "${BLUE}🔍 Verificando status da aplicação na porta $PORTA...${NC}"
    PID=$(sudo lsof -t -i:$PORTA)

    if [ -n "$PID" ]; then
        echo -e "${GREEN}✅ A aplicação está RODANDO.${NC}"
        echo -e "🔢 PID: $PID"
        echo -e "🕒 Iniciada em: $(ps -p $PID -o lstart=)"
        echo -e "📦 Processo: $(ps -p $PID -o cmd=)"
    else
        echo -e "${RED}❌ A aplicação NÃO está rodando na porta $PORTA.${NC}"
    fi
}

mostrar_menu() {
    clear
    echo -e "${BLUE}========= MENU DE CONTROLE - SPRING PROJECT =========${NC}"
    echo -e "${GREEN}1${NC} - Compilar projeto em /opt"
    echo -e "${GREEN}2${NC} - [Deploy] Copiar projeto da /home → /opt, gerar backup, compilar e reiniciar"
    echo -e "${GREEN}3${NC} - Iniciar aplicação"
    echo -e "${GREEN}4${NC} - Parar aplicação"
    echo -e "${GREEN}5${NC} - Reiniciar aplicação"
    echo -e "${GREEN}6${NC} - Ver últimas linhas do log"
    echo -e "${GREEN}7${NC} - Acompanhar log em tempo real"
    echo -e "${GREEN}8${NC} - Ver status da aplicação"
    echo -e "${RED}0${NC} - Sair"
    echo -e "${BLUE}=====================================================${NC}"
}

# ========== LOOP PRINCIPAL ==========
while true; do
    mostrar_menu
    read -p "Escolha uma opção: " opcao

    case $opcao in
        1) compilar_projeto ;;
        2) copiar_projeto ;;
        3) iniciar_aplicacao ;;
        4) parar_aplicacao ;;
        5) reiniciar_aplicacao ;;
        6) ver_logs ;;
        7) log_em_tempo_real ;;
        8) ver_status ;;
        0) echo -e "${GREEN}👋 Saindo...${NC}"; exit ;;
        *) echo -e "${RED}❌ Opção inválida. Tente novamente.${NC}"; sleep 1 ;;
    esac

    echo -e "\n${YELLOW}Pressione Enter para continuar...${NC}"
    read
done
