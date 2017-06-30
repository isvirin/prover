var translate = {
    'ru': {
        'whitepaper-a-href': '<a title="Whitepaper PDF" target="_blank" href="https://github.com/isvirin/prover/blob/master/WhitePaper_PROVER_(rus).pdf">Whitepaper <i class="fa fa-share-square-o" aria-hidden="true"></i></a>',
        'whitepaper-a-href-btnsquare': '<a target="_blank" href="https://github.com/isvirin/prover/blob/master/WhitePaper_PROVER_(rus).pdf" class="btn-square">Whitepaper</a>',
        'How it work': 'Как это работает',
        'Use cases': 'Области применения',
        'Investing': 'Инвестирование',
        'Team': 'Команда',
        'Contacts': 'Контакты',
        'About technology line': 'Технология, позволяющая подтверждать подлинность создания видеоматериалов',
        'Nadezhda Nabilskaya': 'Набильская Надежда',
        'Alexey Rytikov': 'Рытиков Алексей',
        'Vyacheslav Voronin': 'Воронин Вячеслав',
        'Video analysis': 'Видеоанализ',
        'Vitaly Suprun': 'Супрун Виталий',
        'Applications': 'Приложения',
        'Elena Yuferova': 'Юферова Елена',
        'Business consultant': 'Бизнес-консультант',
        'Evgeny Shumilov': 'Шумилов Евгений',
        'Advisor': 'Советник',
        'Oleg Khovayko': 'Ховайко Олег',
        'Ilya Svirin': 'Свирин Илья'
    },
    'en': {
        'whitepaper-a-href': '<a title="Whitepaper PDF" target="_blank" href="https://github.com/isvirin/prover/blob/master/WhitePaper_PROVER_(eng).pdf">Whitepaper <i class="fa fa-share-square-o" aria-hidden="true"></i></a>',
        'whitepaper-a-href-btnsquare': '<a target="_blank" href="https://github.com/isvirin/prover/blob/master/WhitePaper_PROVER_(eng).pdf" class="btn-square">Whitepaper</a>',
        'How it work': 'How it work',
        'Use cases': 'Use cases',
        'Investing': 'Investing',
        'Team': 'Team',
        'Contacts': 'Contacts',
        'About technology line': 'Technology allows receiving confirmation of the occurrence of events on video',
        'Nadezhda Nabilskaya': 'Nabilskaya Nadejda',
        'Alexey Rytikov': 'Alexey Rytikov',
        'Vyacheslav Voronin': 'Vyacheslav Voronin',
        'Video analysis': 'Video analysis',
        'Vitaly Suprun': 'Vitaly Suprun',
        'Applications': 'Applications',
        'Elena Yuferova': 'Elena Yuferova',
        'Business consultant': 'Business consultant',
        'Evgeny Shumilov': 'Evgeny Shumilov',
        'Advisor': 'Advisor',
        'Oleg Khovayko': 'Oleg Khovayko',
        'Ilya Svirin': 'Ilya Svirin'
    }
};
var lang = navigator.languages && navigator.languages[0] ||
           navigator.language ||
           navigator.userLanguage;
if (localStorage.lang) {
    lang = localStorage.lang;
}
function tr(code) {
    var trArray = translate['en'];
    if (lang === 'ru') {
        trArray = translate['ru'];
    }
    document.write(trArray[code]);
}