package com.mobdeve.s12.mco

class BookGenerator {
    companion object {
        fun generateSampleBooks() : ArrayList<BookModel> {
            val data = ArrayList<BookModel>()
            data.add(BookModel("/works/OL24216892W", "Scarlet Witch", arrayListOf("James Robinson","Vanesa R. Del Rey","Marco Rudy","Steve Dillon","Chris Visions"), arrayListOf("Literature"), "Panini Books", R.drawable.book_scarlet_witch,
                "12F 15D", 2021, HasTransaction.NONE))
            data.add(BookModel("/works/OL261169W", "How the Grinch Stole Christmas!", arrayListOf("Dr. Seuss,Rik Mayall"), arrayListOf("Christmas stories.sh","Pictorial works","Juvenile fiction","Children's stories.sh","Christmas stories","Christmas","Behavior", "fiction","Christmas", "fiction","Children's fiction","Stories in rhyme"), "Random House Children's Books", R.drawable.book_grinch,
            "8F 60F", 1957, HasTransaction.INACTIVE))
            data.add(BookModel("/works/OL29226517W", "Fourth Wing", arrayListOf("Rebecca Yarros"), arrayListOf("Fantasía","Fantasy","ficción","ficciones","Fiction","Romántica","Romance","Romántico","joven adulto","Young adult fiction"), "‎Entangled: Red Tower Books (May 2, 2023)", R.drawable.book_fourth_wing,
                "13F 38T", 2023, HasTransaction.NONE))
            data.add(BookModel("/works/OL8115854W", "Il mio nome è Stilton, Geronimo Stilton", arrayListOf("Elisabetta Dami"), arrayListOf("Juvenile fiction","Mice","Journalists","Fiction","Humorous stories","Souris","Romans, nouvelles, etc. pour la jeunesse","Journalistes","Action & Adventure","Animals"), "Turtleback Books Distributed by Demco Media", R.drawable.book_geronimo_stilton,
                "8F 31W", 1999, HasTransaction.NONE))
            data.add(BookModel("/works/OL260413W", "The Tower of Babel", arrayListOf("Morris West"), arrayListOf("Middle east, fiction","Fiction, war & military","Fiction, political","Israel-arab war, 1967, fiction"), "Hodder & Stoughton General Division", R.drawable.book_tower_of_babel,
                "11F 43K", 1974, HasTransaction.NONE))
            data.add(BookModel("/works/OL412310W", "Snow White", arrayListOf("Brothers Grimm"), arrayListOf(), "Amer School Pub", R.drawable.book_snow_white,
                "10F 93W", 1979, HasTransaction.INACTIVE))
            data.add(BookModel("/works/OL18417W", "The Wonderful Wizard of Oz", arrayListOf("L. Frank Baum"), arrayListOf("Witches","Toy and movable books","Spanish language materials","Fiction","Wizards","Juvenile literature","Wizards in fiction","Children's stories, Russian","Specimens","Imaginary voyages in fiction"), "Henry Holt", R.drawable.book_wizard_of_oz,
                "8F 95G", 1900, HasTransaction.NONE))
            data.add(BookModel("/works/OL45429W", "Little Red Riding Hood", arrayListOf("Charles Perrault,Brothers Grimm"), arrayListOf("Tales","Fairy tales","Juvenile literature","Folklore","Little Red Riding Hood (Tale)","Pictorial works","Germany","Fairy Tales & Folklore - Single Title","Children's Books/Ages 4-8 Fiction","Children: Grades 2-3"), "William Morrow & Company", R.drawable.book_little_red_riding_hood,
                "13F 70E", 1945, HasTransaction.NONE))
            data.add(BookModel("/works/OL5735363W", "The Hunger Games", arrayListOf("Suzanne Collins"), arrayListOf("severe poverty","starvation","oppression","effects of war","self-sacrifice","Science fiction","Apocalyptic fiction","Dystopian fiction","Fiction","Juvenile works"), "Klett Sprachen GmbH", R.drawable.book_hunger_games,
                "13F 33E", 2008, HasTransaction.NONE))
            data.add(BookModel("/works/OL82563W", "Harry Potter and the Philosopher's Stone", arrayListOf("J. K. Rowling"), arrayListOf("Ghosts","Monsters","Vampires","Witches","Challenges and Overcoming Obstacles","Magic and Supernatural","Cleverness","School Life","school stories","Wizards"), "Bloomsbury USA Children's Books", R.drawable.book_harry_potter,
                "9F 94V", 1997, HasTransaction.INACTIVE))

            return data
        }
    }
}



















