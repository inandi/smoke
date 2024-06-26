/**
 * data source
 *
 *
 * @author Gobinda Nandi
 * @version 0.2
 * @since 2024-04-01
 * @copyright Copyright (c) 2024
 * @license This code is licensed under the MIT License.
 * See the LICENSE file for details.
 */

package com.inandi.smoke

class DataSet {

    /**
     * Provides an array containing quit smoking progress data.
     * Each element of the array represents a milestone in the quitting process,
     * containing information such as duration, icon, and description.
     *
     * @return An array of arrays, where each inner array represents a milestone.
     *         The inner array structure: [id, title, award, description, imagePath, hourDuration]
     *         - id: An int representing id of each milestone.
     *         - title: A string representing the title or name of the milestone.
     *         - award: A string representing the icon/award associated with the milestone.
     *         - description: A string providing details about the milestone.
     *         - imagePath: A string providing details location of image.
     *         - hourDuration: A string providing duration in hours.
     * @since 0.1
     */
     fun quitSmokingProgress(): Array<Array<String>> {
        return arrayOf(
            arrayOf(
                "1",
                "After 12 hours",
                "Bee",
                "Your carbon monoxide level in your blood drops to normal. This means more oxygen is reaching your organs and cells throughout your body.",
                "file:///android_asset/media/award/bee.png",
                "12"),

            arrayOf(
                "2",
                "After 1 Day",
                "Rabbit",
                "You'll start to feel a slight improvement in your circulation and your sense of smell and taste may start to return.",
                "file:///android_asset/media/award/rabbit.png",
                "24"),

            arrayOf(
                "3",
                "After 2 Days",
                "Leopard",
                "Your bronchial tubes begin to relax and you may notice breathing feels a little easier.",
                "file:///android_asset/media/award/leopard.png",
                "48"),

            arrayOf(
                "4",
                "After 1 Week",
                "Bear",
                "The carbon monoxide level in your body will continue to drop and your lung function will start to improve. You may also notice you have more energy.",
                "file:///android_asset/media/award/bear.png",
                "168"),

            arrayOf(
                "5",
                "After 2 Weeks",
                "Gorilla",
                "Your circulation will continue to improve and your lung function may increase by up to 30%. This means physical activity will become a little easier.",
                "file:///android_asset/media/award/gorilla.png",
                "336"),

            arrayOf(
                "6",
                "After 1 Month",
                "Wolf",
                "You may experience a heightened sense of taste and smell. You may also cough less frequently and have more energy. In addition, cilia, tiny hairs that help keep your lungs clear of mucus and debris, begin to regenerate.",
                "file:///android_asset/media/award/wolf.png",
                "720"),

            arrayOf(
                "7",
                "After 2 Months",
                "Lion",
                "Your lung function will continue to improve and you may notice a decrease in coughing and shortness of breath. You may also find it easier to manage stress without reaching for a cigarette.",
                "file:///android_asset/media/award/lion.png",
                "1440"),

            arrayOf(
                "8",
                "After 3 Months",
                "Shark",
                "Your fertility will improve (if you're a woman) and the risk of premature birth decreases (if you're pregnant).",
                "file:///android_asset/media/award/shark.png",
                "2160"),

            arrayOf(
                "9",
                "After 6 Months",
                "Tiger",
                "Your lung function will continue to improve and your immune system will be stronger, helping you fight off infections. You may also cough up less phlegm and mucus.",
                "file:///android_asset/media/award/tiger.png",
                "4320"),

            arrayOf(
                "10",
                "After 1 Year",
                "Eagle",
                "Your risk of coronary heart disease is about half that of a smoker's. Your overall health will continue to improve.",
                "file:///android_asset/media/award/eagle.png",
                "8760"),

            arrayOf(
                "11",
                "After 5 Years",
                "Elephant",
                "Your risk of certain cancers, including mouth, throat, esophageal, and bladder cancer, is reduced by half. Your risk of stroke also returns to that of a non-smoker.",
                "file:///android_asset/media/award/elephant.png",
                "43800"),

            arrayOf(
                "12",
                "After 10 Years",
                "Rhino",
                "Your risk of lung cancer drops to about half that of a smoker's. Your risk of developing other smoking-related cancers, such as esophageal cancer and pancreatic cancer, also continues to decrease.",
                "file:///android_asset/media/award/rhino.png",
                "87600"),

            arrayOf(
                "13",
                "After 15 Years",
                "Dragon",
                "Your risk of coronary heart disease is almost the same as that of a non-smoker. The longer you quit, the more your body continues to heal itself.",
                "file:///android_asset/media/award/dragon.png",
                "131400"),
        )
    }

    /**
     * Initializes the array of countries with their respective currencies and symbols.
     *
     * @return An array of arrays containing country name, currency name, and currency symbol.
     * @since 0.1
     */
     fun initializeCountriesArray(): Array<Array<String>> {
        return arrayOf(
            arrayOf("Afghanistan", "Afghan Afghani", "؋"),
            arrayOf("Albania", "Albanian Lek", "L"),
            arrayOf("Algeria", "Algerian Dinar", "د.ج"),
            arrayOf("Andorra", "Euro", "€"),
            arrayOf("Angola", "Angolan Kwanza", "Kz"),
            arrayOf("Antigua and Barbuda", "Eastern Caribbean Dollar", "$"),
            arrayOf("Argentina", "Argentine Peso", "$"),
            arrayOf("Armenia", "Armenian Dram", "֏"),
            arrayOf("Australia", "Australian Dollar", "$"),
            arrayOf("Austria", "Euro", "€"),
            arrayOf("Azerbaijan", "Azerbaijani Manat", "₼"),
            arrayOf("Bahamas", "Bahamian Dollar", "$"),
            arrayOf("Bahrain", "Bahraini Dinar", "ب.د"),
            arrayOf("Bangladesh", "Bangladeshi Taka", "৳"),
            arrayOf("Barbados", "Barbadian Dollar", "$"),
            arrayOf("Belarus", "Belarusian Ruble", "Br"),
            arrayOf("Belgium", "Euro", "€"),
            arrayOf("Belize", "Belize Dollar", "$"),
            arrayOf("Benin", "West African CFA franc", "Fr"),
            arrayOf("Bhutan", "Bhutanese Ngultrum", "Nu."),
            arrayOf("Bolivia", "Bolivian Boliviano", "Bs."),
            arrayOf("Bosnia and Herzegovina", "Bosnia and Herzegovina Convertible Mark", "KM"),
            arrayOf("Botswana", "Botswana Pula", "P"),
            arrayOf("Brazil", "Brazilian Real", "R$"),
            arrayOf("Brunei", "Brunei Dollar", "$"),
            arrayOf("Bulgaria", "Bulgarian Lev", "лв"),
            arrayOf("Burkina Faso", "West African CFA franc", "Fr"),
            arrayOf("Burundi", "Burundian Franc", "Fr"),
            arrayOf("Cabo Verde", "Cape Verdean Escudo", "$"),
            arrayOf("Cambodia", "Cambodian Riel", "៛"),
            arrayOf("Cameroon", "Central African CFA franc", "Fr"),
            arrayOf("Canada", "Canadian Dollar", "$"),
            arrayOf("Central African Republic", "Central African CFA franc", "Fr"),
            arrayOf("Chad", "Central African CFA franc", "Fr"),
            arrayOf("Chile", "Chilean Peso", "$"),
            arrayOf("China", "Chinese Yuan", "¥"),
            arrayOf("Colombia", "Colombian Peso", "$"),
            arrayOf("Comoros", "Comorian Franc", "Fr"),
            arrayOf("Congo (Brazzaville)", "Central African CFA franc", "Fr"),
            arrayOf("Congo (Kinshasa)", "Congolese Franc", "FC"),
            arrayOf("Costa Rica", "Costa Rican Colon", "₡"),
            arrayOf("Croatia", "Croatian Kuna", "kn"),
            arrayOf("Cuba", "Cuban Peso", "$"),
            arrayOf("Cyprus", "Euro", "€"),
            arrayOf("Czech Republic", "Czech Koruna", "Kč"),
            arrayOf("Denmark", "Danish Krone", "kr"),
            arrayOf("Djibouti", "Djiboutian Franc", "Fr"),
            arrayOf("Dominica", "Eastern Caribbean Dollar", "$"),
            arrayOf("Dominican Republic", "Dominican Peso", "$"),
            arrayOf("Ecuador", "United States Dollar", "$"),
            arrayOf("Egypt", "Egyptian Pound", "E£"),
            arrayOf("El Salvador", "United States Dollar", "$"),
            arrayOf("Equatorial Guinea", "Central African CFA franc", "Fr"),
            arrayOf("Eritrea", "Eritrean Nakfa", "Nfk"),
            arrayOf("Estonia", "Euro", "€"),
            arrayOf("Eswatini", "Swazi Lilangeni", "E"),
            arrayOf("Ethiopia", "Ethiopian Birr", "Br"),
            arrayOf("Fiji", "Fijian Dollar", "$"),
            arrayOf("Finland", "Euro", "€"),
            arrayOf("France", "Euro", "€"),
            arrayOf("Gabon", "Central African CFA franc", "Fr"),
            arrayOf("Gambia", "Gambian Dalasi", "D"),
            arrayOf("Georgia", "Georgian Lari", "ლ"),
            arrayOf("Germany", "Euro", "€"),
            arrayOf("Ghana", "Ghanaian Cedi", "₵"),
            arrayOf("Greece", "Euro", "€"),
            arrayOf("Grenada", "Eastern Caribbean Dollar", "$"),
            arrayOf("Guatemala", "Guatemalan Quetzal", "Q"),
            arrayOf("Guinea", "Guinean Franc", "Fr"),
            arrayOf("Guinea-Bissau", "West African CFA franc", "Fr"),
            arrayOf("Guyana", "Guyanese Dollar", "$"),
            arrayOf("Haiti", "Haitian Gourde", "G"),
            arrayOf("Honduras", "Honduran Lempira", "L"),
            arrayOf("Hungary", "Hungarian Forint", "Ft"),
            arrayOf("Iceland", "Icelandic Krona", "kr"),
            arrayOf("India", "Indian Rupee", "₹"),
            arrayOf("Indonesia", "Indonesian Rupiah", "Rp"),
            arrayOf("Iran", "Iranian Rial", "﷼"),
            arrayOf("Iraq", "Iraqi Dinar", "ع.د"),
            arrayOf("Ireland", "Euro", "€"),
            arrayOf("Israel", "Israeli Shekel", "₪"),
            arrayOf("Italy", "Euro", "€"),
            arrayOf("Ivory Coast", "West African CFA franc", "Fr"),
            arrayOf("Jamaica", "Jamaican Dollar", "J$"),
            arrayOf("Japan", "Japanese Yen", "¥"),
            arrayOf("Jordan", "Jordanian Dinar", "د.ا"),
            arrayOf("Kazakhstan", "Kazakhstani Tenge", "₸"),
            arrayOf("Kenya", "Kenyan Shilling", "Sh"),
            arrayOf("Kiribati", "Australian Dollar", "$"),
            arrayOf("Kuwait", "Kuwaiti Dinar", "د.ك"),
            arrayOf("Kyrgyzstan", "Kyrgyzstani Som", "сом"),
            arrayOf("Laos", "Lao Kip", "₭"),
            arrayOf("Latvia", "Euro", "€"),
            arrayOf("Lebanon", "Lebanese Pound", "ل.ل"),
            arrayOf("Lesotho", "Lesotho Loti", "L"),
            arrayOf("Liberia", "Liberian Dollar", "$"),
            arrayOf("Libya", "Libyan Dinar", "ل.د"),
            arrayOf("Liechtenstein", "Swiss Franc", "CHF"),
            arrayOf("Lithuania", "Euro", "€"),
            arrayOf("Luxembourg", "Euro", "€"),
            arrayOf("Madagascar", "Malagasy Ariary", "Ar"),
            arrayOf("Malawi", "Malawian Kwacha", "MK"),
            arrayOf("Malaysia", "Malaysian Ringgit", "RM"),
            arrayOf("Maldives", "Maldivian Rufiyaa", "ރ."),
            arrayOf("Mali", "West African CFA franc", "Fr"),
            arrayOf("Malta", "Euro", "€"),
            arrayOf("Marshall Islands", "United States Dollar", "$"),
            arrayOf("Mauritania", "Mauritanian Ouguiya", "UM"),
            arrayOf("Mauritius", "Mauritian Rupee", "₨"),
            arrayOf("Mexico", "Mexican Peso", "$"),
            arrayOf("Micronesia", "United States Dollar", "$"),
            arrayOf("Moldova", "Moldovan Leu", "L"),
            arrayOf("Monaco", "Euro", "€"),
            arrayOf("Mongolia", "Mongolian Tugrik", "₮"),
            arrayOf("Montenegro", "Euro", "€"),
            arrayOf("Morocco", "Moroccan Dirham", "د.م."),
            arrayOf("Mozambique", "Mozambican Metical", "MT"),
            arrayOf("Myanmar", "Myanmar Kyat", "K"),
            arrayOf("Namibia", "Namibian Dollar", "$"),
            arrayOf("Nauru", "Australian Dollar", "$"),
            arrayOf("Nepal", "Nepalese Rupee", "₨"),
            arrayOf("Netherlands", "Euro", "€"),
            arrayOf("New Zealand", "New Zealand Dollar", "$"),
            arrayOf("Nicaragua", "Nicaraguan Cordoba", "C$"),
            arrayOf("Niger", "West African CFA franc", "Fr"),
            arrayOf("Nigeria", "Nigerian Naira", "₦"),
            arrayOf("North Korea", "North Korean Won", "₩"),
            arrayOf("North Macedonia", "Macedonian Denar", "ден"),
            arrayOf("Norway", "Norwegian Krone", "kr"),
            arrayOf("Oman", "Omani Rial", "ر.ع."),
            arrayOf("Pakistan", "Pakistani Rupee", "₨"),
            arrayOf("Palau", "United States Dollar", "$"),
            arrayOf("Palestine", "Israeli Shekel", "₪"),
            arrayOf("Panama", "Panamanian Balboa", "B/."),
            arrayOf("Papua New Guinea", "Papua New Guinean Kina", "K"),
            arrayOf("Paraguay", "Paraguayan Guarani", "₲"),
            arrayOf("Peru", "Peruvian Sol", "S/"),
            arrayOf("Philippines", "Philippine Peso", "₱"),
            arrayOf("Poland", "Polish Zloty", "zł"),
            arrayOf("Portugal", "Euro", "€"),
            arrayOf("Qatar", "Qatari Riyal", "ر.ق"),
            arrayOf("Romania", "Romanian Leu", "lei"),
            arrayOf("Russia", "Russian Ruble", "₽"),
            arrayOf("Rwanda", "Rwandan Franc", "Fr"),
            arrayOf("Saint Kitts and Nevis", "Eastern Caribbean Dollar", "$"),
            arrayOf("Saint Lucia", "Eastern Caribbean Dollar", "$"),
            arrayOf("Saint Vincent and the Grenadines", "Eastern Caribbean Dollar", "$"),
            arrayOf("Samoa", "Samoan Tala", "T"),
            arrayOf("San Marino", "Euro", "€"),
            arrayOf("Sao Tome and Principe", "São Tomé and Príncipe Dobra", "Db"),
            arrayOf("Saudi Arabia", "Saudi Riyal", "ر.س"),
            arrayOf("Senegal", "West African CFA franc", "Fr"),
            arrayOf("Serbia", "Serbian Dinar", "дин."),
            arrayOf("Seychelles", "Seychellois Rupee", "₨"),
            arrayOf("Sierra Leone", "Sierra Leonean Leone", "Le"),
            arrayOf("Singapore", "Singapore Dollar", "$"),
            arrayOf("Slovakia", "Euro", "€"),
            arrayOf("Slovenia", "Euro", "€"),
            arrayOf("Solomon Islands", "Solomon Islands Dollar", "$"),
            arrayOf("Somalia", "Somali Shilling", "Sh"),
            arrayOf("South Africa", "South African Rand", "R"),
            arrayOf("South Korea", "South Korean Won", "₩"),
            arrayOf("South Sudan", "South Sudanese Pound", "£"),
            arrayOf("Spain", "Euro", "€"),
            arrayOf("Sri Lanka", "Sri Lankan Rupee", "₨"),
            arrayOf("Sudan", "Sudanese Pound", "£"),
            arrayOf("Suriname", "Surinamese Dollar", "$"),
            arrayOf("Sweden", "Swedish Krona", "kr"),
            arrayOf("Switzerland", "Swiss Franc", "CHF"),
            arrayOf("Syria", "Syrian Pound", "£"),
            arrayOf("Taiwan", "New Taiwan Dollar", "NT$"),
            arrayOf("Tajikistan", "Tajikistani Somoni", "ЅМ"),
            arrayOf("Tanzania", "Tanzanian Shilling", "Sh"),
            arrayOf("Thailand", "Thai Baht", "฿"),
            arrayOf("Timor-Leste", "United States Dollar", "$"),
            arrayOf("Togo", "West African CFA franc", "Fr"),
            arrayOf("Tonga", "Tongan Pa'anga", "T$"),
            arrayOf("Trinidad and Tobago", "Trinidad and Tobago Dollar", "TT$"),
            arrayOf("Tunisia", "Tunisian Dinar", "د.ت"),
            arrayOf("Turkey", "Turkish Lira", "₺"),
            arrayOf("Turkmenistan", "Turkmenistan Manat", "T"),
            arrayOf("Tuvalu", "Australian Dollar", "$"),
            arrayOf("Uganda", "Ugandan Shilling", "Sh"),
            arrayOf("Ukraine", "Ukrainian Hryvnia", "₴"),
            arrayOf("United Arab Emirates", "United Arab Emirates Dirham", "د.إ"),
            arrayOf("United Kingdom", "Pound Sterling", "£"),
            arrayOf("United States", "United States Dollar", "$"),
            arrayOf("Uruguay", "Uruguayan Peso", "$"),
            arrayOf("Uzbekistan", "Uzbekistani Som", "so'm"),
            arrayOf("Vanuatu", "Vanuatu Vatu", "Vt"),
            arrayOf("Vatican City", "Euro", "€"),
            arrayOf("Venezuela", "Venezuelan Bolivar", "Bs."),
            arrayOf("Vietnam", "Vietnamese Dong", "₫"),
            arrayOf("Yemen", "Yemeni Rial", "﷼"),
            arrayOf("Zambia", "Zambian Kwacha", "ZK"),
            arrayOf("Zimbabwe", "Zimbabwean Dollar", "$")
        )
    }


}