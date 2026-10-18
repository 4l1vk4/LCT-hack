# -*- coding: utf-8 -*-
import json
import os
import shutil

from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.lib import colors
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.pdfgen import canvas

# Регистрация кириллических шрифтов
FONT_DIR = "/System/Library/Fonts/Supplemental/"
pdfmetrics.registerFont(TTFont("Arial", os.path.join(FONT_DIR, "Arial.ttf")))
pdfmetrics.registerFont(TTFont("Arial-Bold", os.path.join(FONT_DIR, "Arial Bold.ttf")))
pdfmetrics.registerFont(TTFont("Arial-Italic", os.path.join(FONT_DIR, "Arial Italic.ttf")))
pdfmetrics.registerFont(TTFont("Arial-BoldItalic", os.path.join(FONT_DIR, "Arial Bold Italic.ttf")))

pdfmetrics.registerFontFamily(
    "Arial",
    normal="Arial",
    bold="Arial-Bold",
    italic="Arial-Italic",
    boldItalic="Arial-BoldItalic"
)

# Цветовая палитра FinPet
PRIMARY_DARK = colors.HexColor("#26150B")
PRIMARY_WOOD = colors.HexColor("#6B3C1A")
ACCENT_ORANGE = colors.HexColor("#E65100")
ACCENT_GREEN = colors.HexColor("#2E7D32")
ACCENT_RED = colors.HexColor("#C62828")
ACCENT_BLUE = colors.HexColor("#1565C0")
BG_PARCHMENT = colors.HexColor("#FFF8E7")
BG_PARCHMENT_DARK = colors.HexColor("#F5E4BE")
BORDER_COLOR = colors.HexColor("#C7A86E")
TEXT_MUTED = colors.HexColor("#5D4037")

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super(NumberedCanvas, self).__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_number(num_pages)
            canvas.Canvas.showPage(self)
        canvas.Canvas.save(self)

    def draw_page_number(self, page_count):
        self.saveState()
        self.setFont("Arial", 8.5)
        self.setFillColor(TEXT_MUTED)
        
        # Нижний колонтитул
        page_str = f"Страница {self._pageNumber} из {page_count}"
        self.drawRightString(A4[0] - 15 * mm, 12 * mm, page_str)
        self.drawString(15 * mm, 12 * mm, "FinPet • Школа финансовой грамотности • Сборник заданий и эталонных ответов")
        
        self.setStrokeColor(BORDER_COLOR)
        self.setLineWidth(0.5)
        self.line(15 * mm, 16 * mm, A4[0] - 15 * mm, 16 * mm)
        self.restoreState()


def get_quest_reward(q):
    if 'rewardCoins' in q and q['rewardCoins'] > 0:
        return q['rewardCoins']
    options = q.get('options', [])
    if options:
        return max([opt.get('rewardCoins', 0) for opt in options], default=40)
    return 40


def create_pdf(output_path):
    doc = SimpleDocTemplate(
        output_path,
        pagesize=A4,
        leftMargin=14 * mm,
        rightMargin=14 * mm,
        topMargin=15 * mm,
        bottomMargin=20 * mm
    )

    # Стили текста
    title_style = ParagraphStyle(
        "DocTitle",
        fontName="Arial-Bold",
        fontSize=16,
        leading=19,
        textColor=PRIMARY_DARK,
        alignment=1, # Center
        spaceAfter=3
    )

    subtitle_style = ParagraphStyle(
        "DocSubTitle",
        fontName="Arial",
        fontSize=10,
        leading=13,
        textColor=PRIMARY_WOOD,
        alignment=1,
        spaceAfter=8
    )

    h1_style = ParagraphStyle(
        "StageH1",
        fontName="Arial-Bold",
        fontSize=13.5,
        leading=17,
        textColor=PRIMARY_DARK,
        spaceBefore=10,
        spaceAfter=5
    )

    h2_style = ParagraphStyle(
        "QuestH2",
        fontName="Arial-Bold",
        fontSize=11,
        leading=14,
        textColor=PRIMARY_WOOD,
        spaceBefore=5,
        spaceAfter=3
    )

    body_style = ParagraphStyle(
        "Body",
        fontName="Arial",
        fontSize=8.5,
        leading=12,
        textColor=PRIMARY_DARK
    )

    body_bold = ParagraphStyle(
        "BodyBold",
        fontName="Arial-Bold",
        fontSize=8.5,
        leading=12,
        textColor=PRIMARY_DARK
    )

    situation_style = ParagraphStyle(
        "Situation",
        fontName="Arial-Italic",
        fontSize=8.5,
        leading=12.5,
        textColor=PRIMARY_DARK
    )

    badge_style = ParagraphStyle(
        "Badge",
        fontName="Arial-Bold",
        fontSize=8,
        leading=10,
        textColor=PRIMARY_DARK
    )

    # Чтение квестов
    json_path = "app/src/main/assets/content/quests.json"
    with open(json_path, "r", encoding="utf-8") as f:
        quests = json.load(f)

    story = []

    # --- ТИТУЛЬНЫЙ БЛОК ---
    header_data = [
        [
            Paragraph("FINPET • ШКОЛА ФИНАНСОВОЙ ГРАМОТНОСТИ", ParagraphStyle("HeaderTop", fontName="Arial-Bold", fontSize=9.5, textColor=ACCENT_ORANGE)),
            Paragraph("ДЕПАРТАМЕНТ ФИНАНСОВ ГОРОДА МОСКВЫ", ParagraphStyle("HeaderRight", fontName="Arial-Bold", fontSize=8.5, textColor=TEXT_MUTED, alignment=2))
        ]
    ]
    header_table = Table(header_data, colWidths=[110*mm, 72*mm])
    header_table.setStyle(TableStyle([
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('BOTTOMPADDING', (0,0), (-1,-1), 0),
        ('TOPPADDING', (0,0), (-1,-1), 0),
    ]))
    story.append(header_table)
    story.append(HRFlowable(width="100%", thickness=1.2, color=PRIMARY_WOOD, spaceBefore=3, spaceAfter=6))

    story.append(Paragraph("Сборник заданий, механик и эталонных ответов", title_style))
    story.append(Paragraph("Система обучающих квестов по стадиям взросления питомца (Малыш • Подросток • Взрослый кот)", subtitle_style))

    # Вводная информационная плашка
    intro_text = (
        "<b>Методологическое соответствие ТЗ (п. 2.5.8):</b> Задания представляют собой игровые ситуации с выбором и последствиями "
        "и <b>не ограничиваются</b> классическим тестом с 2 кнопками. В приложении реализованы 4 интерактивные механики: "
        "<b>1) Выбор решения (CHOICE)</b>, <b>2) Ввод финансового термина (TEXT_INPUT)</b>, "
        "<b>3) Распределение монет слайдером (MONEY_ALLOCATION)</b> и <b>4) Расчет сдачи и выгоды (CALCULATION)</b>. "
        "В демонстрационном режиме все 18 заданий всех стадий доступны сразу.<br/>"
        "<b>Элементы рандома:</b> Динамические вариации ситуаций (dynamicVariants с кнопкой «Другая ситуация»), случайное перемешивание вариантов ответов, "
        "а также кнопка быстрого случайного урока на главном экране школы.<br/>"
        "<b>Экономика:</b> За верный выбор начисляется полная награда (+40..50 монет), ошибочный выбор не даёт монет (0 монет), но сопровождается подробным разбором. "
        "Интерфейс заданий оформлен в аутентичном пиксельном стиле без эмодзи."
    )
    intro_table = Table([[Paragraph(intro_text, body_style)]], colWidths=[182*mm])
    intro_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), BG_PARCHMENT),
        ('BOX', (0,0), (-1,-1), 1, BORDER_COLOR),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(intro_table)
    story.append(Spacer(1, 4))

    # --- СВОДНАЯ МАТРИЦА КВЕСТОВ ---
    story.append(Paragraph("Сводная матрица заданий по стадиям взросления", h2_style))
    summary_data = [
        [
            Paragraph("№", badge_style),
            Paragraph("Стадия (возраст)", badge_style),
            Paragraph("Тема ТЗ", badge_style),
            Paragraph("Название задания", badge_style),
            Paragraph("Механика", badge_style),
            Paragraph("Награда", badge_style)
        ]
    ]

    stage_names = {
        "BABY": "1: Малыш (7-8 лет)",
        "TEEN": "2: Подросток (9-10 лет)",
        "ADULT": "3: Взрослый (11+ лет)"
    }
    type_names = {
        "CHOICE": "Выбор решения",
        "TEXT_INPUT": "Ввод термина",
        "MONEY_ALLOCATION": "Слайдер монет",
        "CALCULATION": "Расчет / Сдача"
    }

    for idx, q in enumerate(quests, start=1):
        reward = get_quest_reward(q)
        summary_data.append([
            Paragraph(str(idx), body_style),
            Paragraph(stage_names.get(q['stage'], q['stage']), body_style),
            Paragraph(q['topicTitle'], body_style),
            Paragraph(f"<b>{q['title']}</b>", body_style),
            Paragraph(type_names.get(q['type'], q['type']), body_style),
            Paragraph(f"+{reward} м.", body_bold)
        ])

    summary_table = Table(summary_data, colWidths=[6*mm, 39*mm, 34*mm, 54*mm, 33*mm, 16*mm])
    summary_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), BG_PARCHMENT_DARK),
        ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('TOPPADDING', (0,0), (-1,-1), 1.5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 1.5),
        ('LEFTPADDING', (0,0), (-1,-1), 3),
        ('RIGHTPADDING', (0,0), (-1,-1), 3),
    ]))
    story.append(summary_table)
    story.append(PageBreak())

    # --- ДЕТАЛЬНЫЙ РАЗДЕЛ ПО СТАДИЯМ ---
    stages_order = [
        ("BABY", "СТАДИЯ 1: «МАЛЫШ» (Младший школьный возраст, 7–8 лет)",
         "Образовательная цель стадии: познакомить ребёнка с базовой ролью карманных денег, научить отличать обязательные потребности питомца (питание, здоровье) от минутных забав, сформировать привычку откладывать в копилку и проверять сдачу на кассе."),
        ("TEEN", "СТАДИЯ 2: «ПОДРОСТОК» (Средний возраст, 9–10 лет)",
         "Образовательная цель стадии: сформировать понимание личного бюджета на игровой период, противостоять маркетинговым акциям («3 по цене 2»), осознать назначение финансовой подушки безопасности на непредвиденный случай и сохранять целевые накопления."),
        ("ADULT", "СТАДИЯ 3: «ВЗРОСЛЫЙ КОТ» (Старший возраст, 11+ лет)",
         "Образовательная цель стадии: развить навыки взрослого финансового поведения — инвестиции в полезные знания и навыки, правило 24 часов против спонтанных трат, расчет реальной выгоды кэшбэка и цифровая безопасность (защита от мошенников и секретные СМС-коды).")
    ]

    for stage_key, stage_title, stage_desc in stages_order:
        story.append(Paragraph(stage_title, h1_style))
        story.append(Paragraph(stage_desc, ParagraphStyle("StageDesc", fontName="Arial-Italic", fontSize=8.5, leading=12.5, textColor=TEXT_MUTED, spaceAfter=8)))

        stage_quests = [q for q in quests if q['stage'] == stage_key]

        for q in stage_quests:
            reward = get_quest_reward(q)

            # Заголовок карточки
            header_cell = [
                Paragraph(f"<b>{q['title']}</b>", ParagraphStyle("QT", fontName="Arial-Bold", fontSize=10.5, textColor=PRIMARY_DARK)),
                Paragraph(f"Тема: <b>{q['topicTitle']}</b>  •  Механика: <b>{type_names.get(q['type'], q['type'])}</b>  •  Награда: <b>+{reward} монет</b>",
                          ParagraphStyle("QSub", fontName="Arial", fontSize=8, textColor=PRIMARY_WOOD))
            ]

            # Ситуация
            sit_text = f"<b>Игровая ситуация:</b> {q['situation']}"
            if q.get('dynamicVariants') and len(q['dynamicVariants']) > 1:
                sit_text += f"<br/><font color='#6B3C1A'><i>[Случайный вариант]: «{q['dynamicVariants'][1]}»</i></font>"

            content_cells = [Paragraph(sit_text, situation_style), Spacer(1, 3)]

            if q['type'] == 'CHOICE':
                content_cells.append(Paragraph("<b>Варианты ответа и последствия:</b>", body_bold))
                for opt in q.get('options', []):
                    is_rec = opt.get('isRecommended', False)
                    mark = "<b>[ВЕРНЫЙ ВЫБОР]</b>" if is_rec else "<b>[ОШИБОЧНЫЙ ВЫБОР]</b>"
                    opt_color = ACCENT_GREEN if is_rec else ACCENT_RED
                    reward_text = f"+{opt.get('rewardCoins', 0)} монет" if opt.get('rewardCoins', 0) > 0 else "0 монет (без награды)"
                    opt_text = (
                        f"<font color='{opt_color.hexval()}'>{mark}</font> <b>«{opt['text']}»</b> (награда: <b>{reward_text}</b>)<br/>"
                        f"<i>Объяснение питомца:</i> {opt['feedback']}"
                    )
                    content_cells.append(Paragraph(opt_text, body_style))
                    content_cells.append(Spacer(1, 2))

            elif q['type'] == 'TEXT_INPUT':
                answers_str = ", ".join([f"«{a}»" for a in q.get('expectedAnswers', [])])
                text_content = (
                    f"<b>Правильный ответ (термин):</b> <font color='#2E7D32'><b>{answers_str}</b></font> (награда: <b>+{reward} монет</b>)<br/>"
                    f"<b>Подсказка для ребёнка:</b> <i>{q.get('hint', '')}</i><br/>"
                    f"<b>Плейсхолдер в поле ввода:</b> {q.get('inputPlaceholder', '')}<br/>"
                    f"<b>Разбор при правильном ответе:</b> {q.get('feedbackSuccess', '')}<br/>"
                    f"<b>Мягкий разбор при неточном ответе:</b> {q.get('feedbackFailure', '')} <i>(награда: <b>0 монет</b>, ребёнка не стыдят)</i>"
                )
                content_cells.append(Paragraph(text_content, body_style))

            elif q['type'] == 'MONEY_ALLOCATION':
                alloc_text = (
                    f"<b>Сумма для распределения:</b> {q.get('totalBudget', 100)} монет (награда за баланс: <b>+{reward} монет</b>)<br/>"
                    f"<b>Вопрос слайдера:</b> {q.get('sliderLabel', '')}<br/>"
                    f"<b>Рекомендуемый баланс:</b> от <b>{q.get('recommendedMin', 30)}</b> до <b>{q.get('recommendedMax', 70)}</b> монет в копилку/резерв (30–70% от суммы).<br/>"
                    f"<b>Разбор при сбалансированном решении:</b> <font color='#2E7D32'>{q.get('allocationSuccessFeedback', '')}</font><br/>"
                    f"<b>Разбор при крайних решениях (0% или 100%):</b> {q.get('allocationWarningFeedback', '')} <i>(награда: <b>0 монет</b>)</i>"
                )
                content_cells.append(Paragraph(alloc_text, body_style))

            elif q['type'] == 'CALCULATION':
                calc_text = (
                    f"<b>Условия задачи:</b> Базовая цена = {q.get('calculationBasePrice', 30)} монет, "
                    f"Скидка/кэшбэк = {q.get('calculationDiscount', 10)} монет, Внесено = {q.get('calculationGivenMoney', 50)} монет.<br/>"
                    f"<b>Варианты расчёта:</b><br/>"
                )
                content_cells.append(Paragraph(calc_text, body_style))
                for opt in q.get('options', []):
                    is_rec = opt.get('isRecommended', False)
                    mark = "<b>[ВЕРНЫЙ РАСЧЕТ]</b>" if is_rec else "<b>[ОШИБКА РАСЧЕТА]</b>"
                    color_h = "#2E7D32" if is_rec else "#C62828"
                    reward_calc = f"+{opt.get('rewardCoins', 0)} монет" if opt.get('rewardCoins', 0) > 0 else "0 монет"
                    content_cells.append(Paragraph(
                        f"<font color='{color_h}'>{mark}</font> <b>«{opt['text']}»</b> (награда: <b>{reward_calc}</b>) — {opt['feedback']}", body_style
                    ))

            # Таблица-карточка
            card_table = Table([[header_cell], [content_cells]], colWidths=[182*mm])
            card_table.setStyle(TableStyle([
                ('BACKGROUND', (0,0), (-1,0), BG_PARCHMENT_DARK),
                ('BACKGROUND', (0,1), (-1,1), BG_PARCHMENT),
                ('BOX', (0,0), (-1,-1), 1, BORDER_COLOR),
                ('LINEBELOW', (0,0), (-1,0), 1, BORDER_COLOR),
                ('TOPPADDING', (0,0), (-1,-1), 5),
                ('BOTTOMPADDING', (0,0), (-1,-1), 5),
                ('LEFTPADDING', (0,0), (-1,-1), 7),
                ('RIGHTPADDING', (0,0), (-1,-1), 7),
            ]))

            story.append(KeepTogether([card_table, Spacer(1, 6)]))

        story.append(Spacer(1, 8))

    story.append(PageBreak())

    # --- РАЗДЕЛ: ФИНАНСОВЫЙ СЛОВАРИК FINPET ---
    story.append(Paragraph("Финансовый словарик FinPet для детей", h1_style))
    story.append(Paragraph(
        "Все термины адаптированы для восприятия детьми младшего школьного возраста и доступны в интерактивном окне «Словарик Финни» прямо в локации Школы города.",
        ParagraphStyle("GlossaryIntro", fontName="Arial-Italic", fontSize=8.5, leading=12.5, textColor=TEXT_MUTED, spaceAfter=8)
    ))

    glossary_terms = [
        ("Бюджет", "Это план, куда потратить деньги. Ты заранее решаешь, сколько монет на что пойдёт, чтобы не оказаться на мели."),
        ("Доход", "Это деньги, которые ты получаешь. Например, еженедельные карманные или награда за помощь по дому и уроки в школе."),
        ("Расход", "Это монеты, которые ты отдаёшь за покупки товаров или услуг в магазине."),
        ("Обязательные траты", "То, без чего питомец не может обойтись: полезная еда, чистая вода, мисочка, визит к ветеринару."),
        ("Желаемые траты", "То, что приятно и весело, но можно отложить: лакомства, украшения, наклейки, праздничные шляпы."),
        ("Накопления", "Монеты, которые ты откладываешь в сторону и не тратишь прямо сейчас. Они растут и приближают мечту."),
        ("Копилка", "Безопасное место для сбережений. Монеты из копилки нельзя тратить на случайные пустяки."),
        ("Финансовая цель", "Конкретная большая мечта, у которой есть цена и срок (например, Уютный Домик для питомца за 150 монет)."),
        ("Сдача", "Монеты, которые продавец возвращает на кассе, если ты дал купюру больше стоимости товара. Их лучше отправлять в копилку!"),
        ("Скидка и акция", "Снижение цены продавцом. Важно помнить: акция выгодна только тогда, когда товар действительно необходим."),
        ("Карманные деньги", "Сумма, которую регулярно доверяют ребёнку родители, чтобы учиться принимать взрослые финансовые решения."),
        ("Импульсивная покупка", "Спонтанная покупка на эмоциях. Часто приводит к разочарованию. Защита — правило 24 часов паузы."),
        ("Подушка безопасности", "Резерв монет на случай непредвиденных поломок или болезни питомца. Спасает от долгов и паники."),
        ("Инвестиции", "Вложение монет и времени в полезные навыки, образование и здоровье, которые принесут пользу и доход в будущем."),
        ("Кэшбэк", "Возврат части потраченных денег обратно на счёт покупателя. Способ умной экономии на обязательных покупках.")
    ]

    glossary_data = [
        [
            Paragraph("Термин", badge_style),
            Paragraph("Понятное определение для ребёнка", badge_style)
        ]
    ]

    for term, definition in glossary_terms:
        glossary_data.append([
            Paragraph(f"<b>{term}</b>", body_bold),
            Paragraph(definition, body_style)
        ])

    glossary_table = Table(glossary_data, colWidths=[45*mm, 137*mm])
    glossary_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), BG_PARCHMENT_DARK),
        ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
        ('TOPPADDING', (0,0), (-1,-1), 3.5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3.5),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
    ]))
    story.append(glossary_table)

    # Генерация документа
    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"PDF successfully generated at: {output_path}")

if __name__ == "__main__":
    out_dir = "dev_info"
    os.makedirs(out_dir, exist_ok=True)
    target_pdf = os.path.join(out_dir, "Школьные_квесты_и_ответы_FinPet.pdf")
    create_pdf(target_pdf)

    # Копия в корень для быстрого доступа
    shutil.copyfile(target_pdf, "FinPet_School_Quests_Handbook.pdf")

    # Копия в каталог артефактов
    artifact_dir = "/Users/macbook/.gemini/antigravity-ide/brain/2b18a0c3-177d-4d56-ba77-a6aa45129627"
    if os.path.exists(artifact_dir):
        shutil.copyfile(target_pdf, os.path.join(artifact_dir, "Школьные_квесты_и_ответы_FinPet.pdf"))
